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

    protected <T> T checkNotNull(T object, String postfix) {
        return Objects.requireNonNull(object, postfix + " required");
    }

    protected <T> T checkValue(T object, Function<T, String> validator) {
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
