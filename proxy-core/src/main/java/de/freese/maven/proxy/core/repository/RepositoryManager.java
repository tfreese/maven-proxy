// Created: 22.07.23
package de.freese.maven.proxy.core.repository;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import de.freese.maven.proxy.core.component.AbstractComponent;

/**
 * @author Thomas Freese
 */
public class RepositoryManager extends AbstractComponent {

    private final Map<String, Repository> repositories = new TreeMap<>();

    public RepositoryManager add(Repository repository) {
        checkNotNull(repository, "Repository");

        if (repositories.containsKey(repository.getName())) {
            throw new IllegalArgumentException("Repository already exist: " + repository.getName());
        }

        repositories.put(repository.getName(), repository);

        return this;
    }

    public Stream<Repository> getRepositories() {
        return repositories.values().stream().filter(Objects::nonNull);
    }

    public Repository getRepository(String name) {
        return repositories.get(name);
    }
}
