// Created: 19.07.23
package de.freese.maven.proxy.core.repository;

import java.util.List;

/**
 * @author Thomas Freese
 */
public interface VirtualRepository extends Repository {

    VirtualRepository add(RemoteRepository repository);

    VirtualRepository add(LocalRepository repository);

    List<Repository> getRepositories();
}
