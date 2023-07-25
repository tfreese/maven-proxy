// Created: 22.07.23
package de.freese.maven.proxy.core.lifecycle;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.freese.maven.proxy.core.component.AbstractComponent;
import de.freese.maven.proxy.core.utils.Locks;

/**
 * @author Thomas Freese
 */
public abstract class AbstractLifecycle extends AbstractComponent implements Lifecycle {

    enum State {
        NEW,
        STARTED,
        STOPPED,
        FAILED
    }

    private final Lock lock = new ReentrantLock();

    private volatile State currentState = State.NEW;

    @Override
    public final void start() throws Exception {
        ensure(State.NEW, State.STOPPED); // check state before taking lock
        Locks.lock(lock);

        try {
            ensure(State.NEW, State.STOPPED); // check again now we have lock

            try {
                getLogger().info("Starting: " + this);

                doStart();

                currentState = State.STARTED;
                getLogger().info("Started: " + this);
            }
            catch (Throwable failure) {
                doFailed("start", failure);
            }
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public final void stop() throws Exception {
        ensure(State.STARTED); // check state before taking lock
        Locks.lock(lock);

        try {
            ensure(State.STARTED); // check again now we have lock

            try {
                getLogger().info("Stopping: " + this);

                doStop();

                currentState = State.STOPPED;

                getLogger().info("Stopped " + this);
            }
            catch (Throwable failure) {
                doFailed("stop", failure);
            }
        }
        finally {
            lock.unlock();
        }
    }

    protected void doFailed(final String operation, final Throwable cause) throws Exception {
        getLogger().error("Lifecycle operation " + operation + " failed", cause);

        currentState = State.FAILED;

        if (cause instanceof Exception) {
            throw (Exception) cause;
        }

        throw new Exception(cause);
    }

    protected void doStart() throws Exception {
        // Empty
    }

    protected void doStop() throws Exception {
        // Empty
    }

    protected boolean isFailed() {
        return is(State.FAILED);
    }

    protected boolean isStarted() {
        return is(State.STARTED);
    }

    protected boolean isStopped() {
        return is(State.STOPPED);
    }

    private void ensure(final State... allowed) {
        for (State allow : allowed) {
            if (is(allow)) {
                return;
            }
        }

        throw new IllegalStateException("Invalid state: " + currentState + "; allowed: " + Arrays.toString(allowed));
    }

    private boolean is(final State state) {
        return currentState == state;
    }
}
