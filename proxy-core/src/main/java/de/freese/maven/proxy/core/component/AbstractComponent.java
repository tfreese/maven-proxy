// Created: 22.07.23
package de.freese.maven.proxy.core.component;

import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Freese
 */
public abstract class AbstractComponent {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final <T> T checkNotNull(final T object, final String postfix) {
        return Objects.requireNonNull(object, postfix + " required");
    }

    protected final <T> T checkValue(final T object, final Function<T, String> validator) {
        String message = validator.apply(object);

        if (message != null && !message.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return object;
    }

    protected Logger getLogger() {
        return logger;
    }
}
