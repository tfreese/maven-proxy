// Created: 22.07.23
package de.freese.maven.proxy.core.lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Thomas Freese
 */
public class LifecycleManager extends AbstractLifecycle {

    private final CopyOnWriteArrayList<Lifecycle> components = new CopyOnWriteArrayList<>();

    public void add(final Lifecycle component) {
        checkNotNull(component, "Lifecycle");

        boolean added = components.addIfAbsent(component);

        if (added) {
            getLogger().trace("Added: {}", component);
        }
    }

    public void add(final LifecycleAware component) {
        checkNotNull(component, "LifecycleAware");

        add(component.getLifecycle());
    }

    public void add(final Lifecycle... components) {
        checkNotNull(components, "Lifecycles");

        for (Lifecycle component : components) {
            add(component);
        }
    }

    public void add(final LifecycleAware... components) {
        checkNotNull(components, "LifecycleAwares");

        for (LifecycleAware component : components) {
            add(component.getLifecycle());
        }
    }

    public void clear() {
        components.clear();

        getLogger().trace("Cleared");
    }

    public void remove(final Lifecycle component) {
        checkNotNull(component, "Lifecycle");

        boolean removed = components.remove(component);

        if (removed) {
            getLogger().trace("Removed: {}", component);
        }
    }

    public void remove(final LifecycleAware component) {
        checkNotNull(component, "LifecycleAware");

        remove(component.getLifecycle());
    }

    public void remove(final Lifecycle... components) {
        checkNotNull(components, "Lifecycles");

        for (Lifecycle component : components) {
            remove(component);
        }
    }

    public void remove(final LifecycleAware... components) {
        checkNotNull(components, "LifecycleAwares");

        for (LifecycleAware component : components) {
            remove(component.getLifecycle());
        }
    }

    public int size() {
        return components.size();
    }

    @Override
    public String toString() {
        return "Maven-Proxy";
    }

    @Override
    protected void doStart() throws Exception {
        int count = components.size();

        getLogger().info("Starting {} components", count);

        List<Throwable> throwables = new ArrayList<>(count);

        for (Lifecycle component : components) {
            try {
                component.start();
            }
            catch (Throwable failure) {
                getLogger().error("Failed to start component: " + component, failure);
                throwables.add(failure);
            }
        }

        maybePropagate(throwables, "start");
    }

    @Override
    protected void doStop() throws Exception {
        int count = components.size();

        getLogger().info("Stopping {} components", count);

        List<Throwable> throwables = new ArrayList<>(count);

        List<Lifecycle> copy = new ArrayList<>(components);
        Collections.reverse(copy);

        for (Lifecycle component : copy) {
            try {
                component.stop();
            }
            catch (Throwable failure) {
                getLogger().error("Failed to stop component: " + component, failure);
                throwables.add(failure);
            }
        }

        maybePropagate(throwables, "stop");
    }

    protected void maybePropagate(final List<Throwable> throwables, final String messagePart) {
        if (throwables.isEmpty()) {
            return;
        }

        String message = "Failed to %s %d components".formatted(messagePart, throwables.size());

        getLogger().error(message);

        if ("start".equalsIgnoreCase(messagePart)) {
            throw new IllegalStateException(message);
        }
    }
}
