// Created: 22.07.23
package de.freese.maven.proxy.core.repository.virtual;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;
import de.freese.maven.proxy.core.utils.HttpMethod;

/**
 * @author Thomas Freese
 */
public class DefaultVirtualRepository extends AbstractRepository {

    private final CopyOnWriteArrayList<Repository> repositories = new CopyOnWriteArrayList<>();

    public DefaultVirtualRepository(final String name) {
        super(name, URI.create("virtual"));
    }

    public DefaultVirtualRepository add(final Repository repository) {
        checkNotNull(repository, "Repository");

        addRepository(repository);

        return this;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    protected boolean doExist(final URI resource) throws Exception {
        boolean exist = false;

        for (Repository repository : getRepositories()) {
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
    protected RepositoryResponse doGetInputStream(final URI resource) throws Exception {
        RepositoryResponse response = null;

        for (Repository repository : getRepositories()) {
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

    private void addRepository(final Repository repository) {
        boolean added = repositories.addIfAbsent(repository);

        if (added) {
            getLogger().trace("Added: {}", repository);
        }
    }

    private List<Repository> getRepositories() {
        return repositories;
    }
}
