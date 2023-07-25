// Created: 22.07.23
package de.freese.maven.proxy.core.repository.remote;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Supplier;

import de.freese.maven.proxy.core.repository.RepositoryResponse;
import de.freese.maven.proxy.core.utils.ProxyUtils;

/**
 * @author Thomas Freese
 */
public class JreHttpRemoteRepository extends AbstractRemoteRepository {

    private final Supplier<HttpClient> httpClientSupplier;

    public JreHttpRemoteRepository(final String name, final URI uri, final Supplier<HttpClient> httpClientSupplier) {
        super(name, uri);

        this.httpClientSupplier = checkNotNull(httpClientSupplier, "Supplier<HttpClient>");
    }

    @Override
    protected boolean doExist(final URI resource) throws Exception {
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
    protected RepositoryResponse doGetInputStream(final URI resource) throws Exception {
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

        return new RepositoryResponse(uri, contentLength, response.body());
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
