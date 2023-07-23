// Created: 22.07.23
package de.freese.maven.proxy.core.repository.virtual;

import java.net.URI;

import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class DefaultVirtualRepository extends AbstractVirtualRepository {

    public DefaultVirtualRepository(final String name) {
        super(name, URI.create("virtual"));
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            return false;
        }

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
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

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
}
