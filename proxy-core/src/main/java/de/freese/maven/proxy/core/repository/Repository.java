// Created: 19.07.23
package de.freese.maven.proxy.core.repository;

import java.io.InputStream;
import java.net.URI;

import de.freese.maven.proxy.core.lifecycle.Lifecycle;
import de.freese.maven.proxy.core.utils.HttpMethod;

/**
 * @author Thomas Freese
 */
public interface Repository extends Lifecycle {

    boolean exist(URI resource) throws Exception;

    RepositoryResponse getInputStream(URI resource) throws Exception;

    /**
     * The name is the context-root.
     */
    String getName();

    URI getUri();

    boolean supports(HttpMethod httpMethod);

    default void write(URI resource, InputStream inputStream) throws Exception {
        throw new UnsupportedOperationException("read only repository: " + getName() + " - " + getUri());
    }
}
