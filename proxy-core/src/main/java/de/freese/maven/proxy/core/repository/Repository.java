// Created: 19.07.23
package de.freese.maven.proxy.core.repository;

import java.net.URI;

import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.lifecycle.Lifecycle;

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

    boolean supports(HttpMethod httpMethod);
}
