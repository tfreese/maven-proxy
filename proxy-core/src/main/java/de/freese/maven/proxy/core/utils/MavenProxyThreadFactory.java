// Created: 21.09.2019
package de.freese.maven.proxy.core.utils;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Thomas Freese
 */
public class MavenProxyThreadFactory implements ThreadFactory {

    private final boolean daemon;

    private final ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

    private final String namePattern;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /**
     * <pre>
     * Defaults:
     * - daemon = true
     * </pre>
     *
     * @param namePattern String; Example: "thread-%d"
     */
    public MavenProxyThreadFactory(final String namePattern) {
        this(namePattern, true);
    }

    /**
     * @param namePattern String; Example: "thread-%d"
     */
    public MavenProxyThreadFactory(final String namePattern, final boolean daemon) {
        super();

        this.namePattern = Objects.requireNonNull(namePattern, "namePattern required");
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(final Runnable r) {
        Thread thread = this.defaultThreadFactory.newThread(r);

        String threadName = String.format(this.namePattern, this.threadNumber.getAndIncrement());
        thread.setName(threadName);

        thread.setDaemon(this.daemon);

        return thread;
    }
}
