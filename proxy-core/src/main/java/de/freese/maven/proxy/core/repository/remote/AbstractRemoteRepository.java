// Created: 22.07.23
package de.freese.maven.proxy.core.repository.remote;

import java.net.URI;
import java.util.Objects;

import de.freese.maven.proxy.core.component.HttpMethod;
import de.freese.maven.proxy.core.repository.AbstractRepository;
import de.freese.maven.proxy.core.repository.RemoteRepository;

/**
 * @author Thomas Freese
 */
public abstract class AbstractRemoteRepository extends AbstractRepository implements RemoteRepository {

    private final URI uri;

    protected AbstractRemoteRepository(final String name, final URI uri) {
        super(name);

        this.uri = Objects.requireNonNull(uri, "uri required");
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean supports(final HttpMethod httpMethod) {
        return HttpMethod.HEAD.equals(httpMethod) || HttpMethod.GET.equals(httpMethod);
    }

    @Override
    public String toString() {
        return getName() + ": " + getUri().toString();
    }

    protected URI createResourceUri(final URI uri, final URI resource) {
        String path = uri.getPath();
        String pathResource = resource.getPath();

        if (path.endsWith("/") && pathResource.startsWith("/")) {
            path += pathResource.substring(1);
        }
        else if (path.endsWith("/") && !pathResource.startsWith("/")) {
            path += pathResource;
        }
        else if (!path.endsWith("/") && pathResource.startsWith("/")) {
            path += pathResource;
        }

        return uri.resolve(path);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        String scheme = uri.getScheme();

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            String msg = "HTTP or HTTPS protocol required: " + scheme;

            getLogger().error(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
