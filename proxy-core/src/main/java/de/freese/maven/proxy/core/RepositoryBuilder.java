// Created: 24.07.23
package de.freese.maven.proxy.core;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.freese.maven.proxy.blobstore.file.FileBlobStore;
import de.freese.maven.proxy.blobstore.jdbc.JdbcBlobStore;
import de.freese.maven.proxy.config.LocalRepoConfig;
import de.freese.maven.proxy.config.RemoteRepoConfig;
import de.freese.maven.proxy.config.StoreConfig;
import de.freese.maven.proxy.config.VirtualRepoConfig;
import de.freese.maven.proxy.core.component.BlobStoreComponent;
import de.freese.maven.proxy.core.component.DatasourceComponent;
import de.freese.maven.proxy.core.component.JreHttpClientComponent;
import de.freese.maven.proxy.core.lifecycle.LifecycleManager;
import de.freese.maven.proxy.core.repository.Repository;
import de.freese.maven.proxy.core.repository.RepositoryManager;
import de.freese.maven.proxy.core.repository.blobstore.BlobStoreRepository;
import de.freese.maven.proxy.core.repository.cached.CachedRepository;
import de.freese.maven.proxy.core.repository.local.FileRepository;
import de.freese.maven.proxy.core.repository.remote.JreHttpRemoteRepository;
import de.freese.maven.proxy.core.repository.virtual.AbstractVirtualRepository;
import de.freese.maven.proxy.core.repository.virtual.DefaultVirtualRepository;

/**
 * @author Thomas Freese
 */
public final class RepositoryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryBuilder.class);

    public static Repository buildLocal(LocalRepoConfig localRepoConfig, LifecycleManager lifecycleManager, RepositoryManager repositoryManager) {
        URI uri = URI.create(localRepoConfig.getPath());

        if ("file".equalsIgnoreCase(uri.getScheme())) {
            Repository repository;

            if (localRepoConfig.isWriteable()) {
                BlobStoreComponent blobStoreComponent = new BlobStoreComponent(new FileBlobStore(uri));
                lifecycleManager.add(blobStoreComponent);

                repository = new BlobStoreRepository(localRepoConfig.getName(), uri, blobStoreComponent.getBlobStore());
            }
            else {
                repository = new FileRepository(localRepoConfig.getName(), uri);
            }

            lifecycleManager.add(repository);
            repositoryManager.add(repository);

            return repository;
        }
        else {
            LOGGER.error("Ignoring LocalRepository '{}', file URI scheme expected: {}", localRepoConfig.getName(), uri);
        }

        return null;
    }

    public static Repository buildRemote(RemoteRepoConfig remoteRepoConfig, LifecycleManager lifecycleManager, RepositoryManager repositoryManager, JreHttpClientComponent httpClientComponent) {
        URI uri = URI.create(remoteRepoConfig.getUrl());

        if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
            Repository repository = new JreHttpRemoteRepository(remoteRepoConfig.getName(), uri, httpClientComponent::getHttpClient);
            lifecycleManager.add(repository);

            if (remoteRepoConfig.getStore() != null) {
                StoreConfig storeConfig = remoteRepoConfig.getStore();

                if ("file".equalsIgnoreCase(storeConfig.getType())) {
                    URI uriCached = URI.create(storeConfig.getUrl());

                    BlobStoreComponent blobStoreComponent = new BlobStoreComponent(new FileBlobStore(uriCached));
                    lifecycleManager.add(blobStoreComponent);

                    repository = new CachedRepository(repository, blobStoreComponent.getBlobStore());
                    lifecycleManager.add(repository);
                }
                else if ("jdbc".equalsIgnoreCase(storeConfig.getType())) {
                    DatasourceComponent datasourceComponent = new DatasourceComponent(storeConfig, remoteRepoConfig.getName());
                    lifecycleManager.add(datasourceComponent);

                    BlobStoreComponent blobStoreComponent = new BlobStoreComponent(new JdbcBlobStore(datasourceComponent::getDataSource));
                    lifecycleManager.add(blobStoreComponent);

                    repository = new CachedRepository(repository, blobStoreComponent.getBlobStore());
                    lifecycleManager.add(repository);
                }
            }

            repositoryManager.add(repository);

            return repository;
        }
        else {
            LOGGER.error("Ignoring RemoteRepository '{}', http/https URI scheme expected: {}", remoteRepoConfig.getName(), uri);
        }

        return null;
    }

    public static Repository buildVirtual(VirtualRepoConfig virtualRepoConfig, LifecycleManager lifecycleManager, RepositoryManager repositoryManager) {
        if (!virtualRepoConfig.getRepositoryNames().isEmpty()) {
            AbstractVirtualRepository virtualRepository = new DefaultVirtualRepository(virtualRepoConfig.getName());

            for (String repositoryName : virtualRepoConfig.getRepositoryNames()) {
                Repository repository = repositoryManager.getRepository(repositoryName);

                if (repository == null) {
                    LOGGER.error("Repository not found or configured: {}", repositoryName);
                    continue;
                }

                if (repository instanceof AbstractVirtualRepository vr) {
                    LOGGER.error("A VirtualRepository can not contain another VirtualRepository: {}", vr.getName());
                    continue;
                }

                virtualRepository.add(repository);
            }

            lifecycleManager.add(virtualRepository);
            repositoryManager.add(virtualRepository);

            return virtualRepository;
        }
        else {
            LOGGER.error("Ignoring VirtualRepository '{}', no repositories configured", virtualRepoConfig.getName());
        }

        return null;
    }

    private RepositoryBuilder() {
        super();
    }
}
