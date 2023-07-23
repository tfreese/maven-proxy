// Created: 22.07.23
package de.freese.maven.proxy.core.repository.remote;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

import de.freese.maven.proxy.core.component.ProxyUtils;
import de.freese.maven.proxy.core.repository.RepositoryResponse;

/**
 * @author Thomas Freese
 */
public class JreHttpRemoteRepository extends AbstractRemoteRepository {

    private final Supplier<HttpClient> httpClientSupplier;

    public JreHttpRemoteRepository(final String name, final URI baseUri, final Supplier<HttpClient> httpClientSupplier) {
        super(name, baseUri);

        this.httpClientSupplier = httpClientSupplier;
    }

    @Override
    public boolean exist(final URI resource) throws Exception {
        if (!isStarted()) {
            return false;
        }

        URI uri = createResourceUri(getUri(), resource);

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(ProxyUtils.HTTP_HEADER_USER_AGENT, "Maven-Proxy")
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build()
                ;
        // @formatter:on

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("exist - Request: {}", request.toString());
        }

        HttpResponse<Void> response = getHttpClient().send(request, HttpResponse.BodyHandlers.discarding());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("exist - Response: {}", response.toString());
        }

        return response.statusCode() == ProxyUtils.HTTP_OK;
    }

    @Override
    public RepositoryResponse getInputStream(final URI resource) throws Exception {
        if (!isStarted()) {
            return null;
        }

        URI uri = createResourceUri(getUri(), resource);

        // @formatter:off
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(ProxyUtils.HTTP_HEADER_USER_AGENT, "Maven-Proxy")
                .GET()
                .build()
                ;
        // @formatter:on

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream - Request: {}", request.toString());
        }

        HttpResponse<InputStream> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("getInputStream - Response: {}", response.toString());
        }

        if (response.statusCode() != ProxyUtils.HTTP_OK) {
            return null;
        }

        long contentLength = response.headers().firstValueAsLong(ProxyUtils.HTTP_HEADER_CONTENT_LENGTH).orElse(0);

        return new RepositoryResponse(uri, contentLength, new BufferedInputStream(response.body()));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        checkNotNull(httpClientSupplier, "HttpClientSupplier");
    }

    protected HttpClient getHttpClient() {
        return httpClientSupplier.get();
    }
}
