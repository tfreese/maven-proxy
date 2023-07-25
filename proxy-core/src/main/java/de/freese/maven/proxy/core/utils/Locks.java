// Created: 22.07.23
package de.freese.maven.proxy.core.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author Thomas Freese
 */
public final class Locks {
    /**
     * Returns locked lock.
     *
     * Uses {@link Lock#tryLock} with timeout of 60 seconds to avoid potential deadlocks.
     */
    public static Lock lock(final Lock lock) {
        try {
            if (!lock.tryLock(60, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to obtain lock after 60 seconds");
            }
        }
        catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return lock;
    }

    /**
     * Returns locked read-lock.
     */
    public static Lock read(final ReadWriteLock readWriteLock) {
        return lock(readWriteLock.readLock());
    }

    /**
     * Returns locked write-lock.
     */
    public static Lock write(final ReadWriteLock readWriteLock) {
        return lock(readWriteLock.writeLock());
    }

    private Locks() {
        super();
    }
}
