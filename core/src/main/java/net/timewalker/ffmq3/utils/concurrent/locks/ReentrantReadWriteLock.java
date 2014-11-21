/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package net.timewalker.ffmq3.utils.concurrent.locks;

import java.util.HashMap;

/**
 * (Stripped down version)
 * ReentrantReadWriteLock
 */
public class ReentrantReadWriteLock implements ReadWriteLock  {

    final ReadLock readerLock_ = new ReadLock(this);
    final WriteLock writerLock_ = new WriteLock(this);

    final Sync sync;

    /**
     * Creates a new {@code ReentrantReadWriteLock} with
     * default (nonfair) ordering properties.
     */
    public ReentrantReadWriteLock() {
        this.sync = new NonfairSync();
    }

    public Lock writeLock() { return writerLock_; }
    public Lock readLock()  { return readerLock_; }

    /**
     * Synchronization implementation for ReentrantReadWriteLock.
     * Subclassed into fair and nonfair versions.
     */
    private abstract static class Sync {
		
		private static final int NONE   = 0;
        private static final int READER = 1;
        private static final int WRITER = 2;

        transient int activeReaders_ = 0;
        transient Thread activeWriter_ = null;
        transient int waitingReaders_ = 0;
        transient int waitingWriters_ = 0;

        /** Number of acquires on write lock by activeWriter_ thread **/
        transient int writeHolds_ = 0;

        /** Number of acquires on read lock by any reader thread **/
        transient HashMap readers_ = new HashMap();

        /** cache/reuse the special Integer value one to speed up readlocks **/
        static final Integer IONE = new Integer(1);

        Sync() { /* Nothing */ }

        /*
           Each of these variants is needed to maintain atomicity
           of wait counts during wait loops. They could be
           made faster by manually inlining each other. We hope that
           compilers do this for us though.
        */

        synchronized boolean startReadFromNewReader() {
            boolean pass = startRead();
            if (!pass) ++waitingReaders_;
            return pass;
        }

        synchronized boolean startWriteFromNewWriter() {
            boolean pass = startWrite();
            if (!pass) ++waitingWriters_;
            return pass;
        }

        synchronized boolean startReadFromWaitingReader() {
            boolean pass = startRead();
            if (pass) --waitingReaders_;
            return pass;
        }

        synchronized boolean startWriteFromWaitingWriter() {
            boolean pass = startWrite();
            if (pass) --waitingWriters_;
            return pass;
        }

        /*
           A bunch of small synchronized methods are needed
           to allow communication from the Lock objects
           back to this object, that serves as controller
         */

        synchronized void cancelledWaitingReader() { --waitingReaders_; }
        synchronized void cancelledWaitingWriter() { --waitingWriters_; }

        boolean allowReader() {
            return (activeWriter_ == null && waitingWriters_ == 0) ||
                activeWriter_ == Thread.currentThread();
        }

        synchronized boolean startRead() {
            Thread t = Thread.currentThread();
            Object c = readers_.get(t);
            if (c != null) { // already held -- just increment hold count
                readers_.put(t, new Integer( ( (Integer) (c)).intValue() + 1));
                ++activeReaders_;
                return true;
            }
            else if (allowReader()) {
                readers_.put(t, IONE);
                ++activeReaders_;
                return true;
            }
            else
                return false;
        }

        synchronized boolean startWrite() {
            if (activeWriter_ == Thread.currentThread()) { // already held; re-acquire
                ++writeHolds_;
                return true;
            }
            else if (writeHolds_ == 0) {
                if (activeReaders_ == 0 ||
                    (readers_.size() == 1 &&
                     readers_.get(Thread.currentThread()) != null)) {
                    activeWriter_ = Thread.currentThread();
                    writeHolds_ = 1;
                    return true;
                }
                else
                    return false;
            }
            else
                return false;
        }

        synchronized int endRead() {
            Thread t = Thread.currentThread();
            Object c = readers_.get(t);
            if (c == null)
                throw new IllegalMonitorStateException();
            --activeReaders_;
            if (c != IONE) { // more than one hold; decrement count
                int h = ( (Integer) (c)).intValue() - 1;
                Integer ih = (h == 1) ? IONE : new Integer(h);
                readers_.put(t, ih);
                return NONE;
            }
            else {
                readers_.remove(t);

                if (writeHolds_ > 0) // a write lock is still held by current thread
                    return NONE;
                else if (activeReaders_ == 0 && waitingWriters_ > 0)
                    return WRITER;
                else
                    return NONE;
            }
        }

        synchronized int endWrite() {
            if (activeWriter_ != Thread.currentThread()) {
                throw new IllegalMonitorStateException();
            }
            --writeHolds_;
            if (writeHolds_ > 0) // still being held
                return NONE;
            else {
                activeWriter_ = null;
                if (waitingReaders_ > 0 && allowReader())
                    return READER;
                else if (waitingWriters_ > 0)
                    return WRITER;
                else
                    return NONE;
            }
        }
    }

    /**
     * Nonfair version of Sync
     */
    private static class NonfairSync extends Sync {
    	//private static final long serialVersionUID = 1L;
        NonfairSync() { /* Nothing */ }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock {

        final ReentrantReadWriteLock lock;

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected ReadLock(ReentrantReadWriteLock lock) {
            if (lock == null) throw new NullPointerException();
            this.lock = lock;
        }

        /**
         * Acquires the read lock.
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately.
         *
         * <p>If the write lock is held by another thread then
         * the current thread becomes disabled for thread scheduling
         * purposes and lies dormant until the read lock has been acquired.
         */
        public void lock() {
            synchronized (this) {
                if (lock.sync.startReadFromNewReader()) return;
                boolean wasInterrupted = Thread.interrupted();
                try {
                    while (true) {
                        try {
                            ReadLock.this.wait();
                        }
                        catch (InterruptedException ex) {
                            wasInterrupted = true;
                            // no need to propagate the potentially masked
                            // signal, since readers are always notified all
                        }
                        if (lock.sync.startReadFromWaitingReader()) return;
                    }
                }
                finally {
                    if (wasInterrupted) Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * Acquires the read lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         *
         * <p>Acquires the read lock if the write lock is not held
         * by another thread and returns immediately.
         *
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of two things happens:
         *
         * <ul>
         *
         * <li>The read lock is acquired by the current thread; or
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         *
         * </ul>
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            InterruptedException ie = null;
            synchronized (this) {
                if (!lock.sync.startReadFromNewReader()) {
                    for (; ; ) {
                        try {
                            ReadLock.this.wait();
                            if (lock.sync.startReadFromWaitingReader())
                                return;
                        }
                        catch (InterruptedException ex) {
                            lock.sync.cancelledWaitingReader();
                            ie = ex;
                            break;
                        }
                    }
                }
            }
            if (ie != null) {
                // fall through outside synch on interrupt.
                // This notification is not really needed here,
                //   but may be in plausible subclasses
                lock.writerLock_.signalWaiters();
                throw ie;
            }
        }

        /**
         * Acquires the read lock only if the write lock is not held by
         * another thread at the time of invocation.
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately with the value
         * {@code true}. Even when this lock has been set to use a
         * fair ordering policy, a call to {@code tryLock()}
         * <em>will</em> immediately acquire the read lock if it is
         * available, whether or not other threads are currently
         * waiting for the read lock.  This &quot;barging&quot; behavior
         * can be useful in certain circumstances, even though it
         * breaks fairness.
         *
         * <p>If the write lock is held by another thread then
         * this method will return immediately with the value
         * {@code false}.
         *
         * @return {@code true} if the read lock was acquired
         */
        public boolean tryLock() {
            return lock.sync.startRead();
        }

        /**
         * Attempts to release this lock.
         *
         * <p> If the number of readers is now zero then the lock
         * is made available for write lock attempts.
         */
        public void unlock() {
            switch (lock.sync.endRead()) {
                case Sync.READER: lock.readerLock_.signalWaiters(); return;
                case Sync.WRITER: lock.writerLock_.signalWaiters(); return;
                default:
                case Sync.NONE: return;
            }
        }

        synchronized void signalWaiters() {
            notifyAll();
        }
    }

    /**
     * The lock returned by method {@link ReentrantReadWriteLock#writeLock}.
     */
    public static class WriteLock implements Lock {

        final ReentrantReadWriteLock lock;

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected WriteLock(ReentrantReadWriteLock lock) {
            if (lock == null) throw new NullPointerException();
            this.lock = lock;
        }

        /**
         * Acquires the write lock.
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         *
         * <p>If the current thread already holds the write lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until the write lock has been acquired, at which
         * time the write lock hold count is set to one.
         */
        public void lock() {
            synchronized (this) {
                if (lock.sync.startWriteFromNewWriter()) return;
                boolean wasInterrupted = Thread.interrupted();
                try {
                    while (true) {
                        try {
                            WriteLock.this.wait();
                        }
                        catch (InterruptedException ex) {
                            wasInterrupted = true;
                            // no need to notify; if we were notified,
                            // we will act as notified, and succeed in
                            // startWrite and return
                        }
                        if (lock.sync.startWriteFromWaitingWriter()) return;
                    }
                }
                finally {
                    if (wasInterrupted) Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * Acquires the write lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately, setting the write lock hold count to
         * one.
         *
         * <p>If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * immediately.
         *
         * <p>If the lock is held by another thread then the current
         * thread becomes disabled for thread scheduling purposes and
         * lies dormant until one of two things happens:
         *
         * <ul>
         *
         * <li>The write lock is acquired by the current thread; or
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         *
         * </ul>
         *
         * <p>If the write lock is acquired by the current thread then the
         * lock hold count is set to one.
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method;
         * or
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the write lock,
         *
         * </ul>
         *
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            InterruptedException ie = null;
            synchronized (this) {
                if (!lock.sync.startWriteFromNewWriter()) {
                    for (; ; ) {
                        try {
                            WriteLock.this.wait();
                            if (lock.sync.startWriteFromWaitingWriter())
                                return;
                        }
                        catch (InterruptedException ex) {
                            lock.sync.cancelledWaitingWriter();
                            WriteLock.this.notify();
                            ie = ex;
                            break;
                        }
                    }
                }
            }
            if (ie != null) {
                // Fall through outside synch on interrupt.
                //  On exception, we may need to signal readers.
                //  It is not worth checking here whether it is strictly necessary.
                lock.readerLock_.signalWaiters();
                throw ie;
            }
        }

        /**
         * Acquires the write lock only if it is not held by another thread
         * at the time of invocation.
         *
         * <p>Acquires the write lock if neither the read nor write lock
         * are held by another thread
         * and returns immediately with the value {@code true},
         * setting the write lock hold count to one. Even when this lock has
         * been set to use a fair ordering policy, a call to
         * {@code tryLock()} <em>will</em> immediately acquire the
         * lock if it is available, whether or not other threads are
         * currently waiting for the write lock.  This &quot;barging&quot;
         * behavior can be useful in certain circumstances, even
         * though it breaks fairness. 
         *
         * <p> If the current thread already holds this lock then the
         * hold count is incremented by one and the method returns
         * {@code true}.
         *
         * <p>If the lock is held by another thread then this method
         * will return immediately with the value {@code false}.
         *
         * @return {@code true} if the lock was free and was acquired
         * by the current thread, or the write lock was already held
         * by the current thread; and {@code false} otherwise.
         */
        public boolean tryLock() {
            return lock.sync.startWrite();
        }

        /**
         * Attempts to release this lock.
         *
         * <p>If the current thread is the holder of this lock then
         * the hold count is decremented. If the hold count is now
         * zero then the lock is released.  If the current thread is
         * not the holder of this lock then {@link
         * IllegalMonitorStateException} is thrown.
         *
         * @throws IllegalMonitorStateException if the current thread does not
         * hold this lock.
         */
        public void unlock() {
            switch (lock.sync.endWrite()) {
                case Sync.READER: lock.readerLock_.signalWaiters(); return;
                case Sync.WRITER: lock.writerLock_.signalWaiters(); return;
                default:
                case Sync.NONE: return;
            }
        }

        synchronized void signalWaiters() {
            notify();
        }
    }
}
