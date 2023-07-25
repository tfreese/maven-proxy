// Created: 23.07.23
package de.freese.maven.proxy.core.repository.virtual;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.utils.HttpMethod;

/**
 * @author Thomas Freese
 */
public abstract class AbstractVirtualRepository extends AbstractRepository {

    private final CopyOnWriteArrayList<Repository> repositories = new CopyOnWriteArrayList<>();

    protected AbstractVirtualRepository(final String name, final URI uri) {
        super(name, uri);
    }

    public <T extends AbstractVirtualRepository> T add(final Repository repository) {
        checkNotNull(repository, "Repository");

        addRepository(repository);

        return (T) this;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    protected void addRepository(Repository repository) {
        boolean added = repositories.addIfAbsent(repository);

        if (added) {
            getLogger().trace("Added: {}", repository);
        }
    }

    protected List<Repository> getRepositories() {
        return repositories;
    }
}
