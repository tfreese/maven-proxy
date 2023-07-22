// Created: 22.07.23
package de.freese.maven.proxy.core.repository.virtual;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.LocalRepository;
import de.freese.maven.proxy.core.repository.RemoteRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;
import de.freese.maven.proxy.core.repository.VirtualRepository;

/**
 * @author Thomas Freese
 */
public class DefaultVirtualRepository extends AbstractRepository implements VirtualRepository {

    private final CopyOnWriteArrayList<Repository> repositories = new CopyOnWriteArrayList<>();

    public DefaultVirtualRepository(final String name) {
        super(name);
    }

    @Override
    public VirtualRepository add(final RemoteRepository repository) {
        checkNotNull(repository, "RemoteRepository");

        addRepository(repository);

        return this;
    }

    @Override
    public VirtualRepository add(final LocalRepository repository) {
        checkNotNull(repository, "LocalRepository");

        addRepository(repository);

        return this;
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            return false;
        }

        boolean exist = false;

        for (Repository repository : this.repositories) {
            try {
                exist = repository.exist(resource);
            }
            catch (Exception ex) {
                getLogger().warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            }

            if (exist) {
                break;
            }
        }

        return exist;
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        RepositoryResponse response = null;

        for (Repository repository : this.repositories) {
            try {
                response = repository.getInputStream(resource);
            }
            catch (Exception ex) {
                getLogger().warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            }

            if (response != null) {
                break;
            }
        }

        return response;
    }

    @Override
    public List<Repository> getRepositories() {
        return List.copyOf(repositories);
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("name=").append(getName());
        sb.append(']');

        return sb.toString();
    }

    protected void addRepository(Repository repository) {
        boolean added = repositories.addIfAbsent(repository);

        if (added) {
            getLogger().trace("Added: {}", repository);
        }
    }
}
