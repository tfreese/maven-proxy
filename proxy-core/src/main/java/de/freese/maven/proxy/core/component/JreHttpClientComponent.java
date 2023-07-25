// Created: 23.07.23
package de.freese.maven.proxy.core.component;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.freese.maven.proxy.config.ClientConfig;
import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;
import de.freese.maven.proxy.core.utils.MavenProxyThreadFactory;
import de.freese.maven.proxy.core.utils.ProxyUtils;

/**
 * @author Thomas Freese
 */
public class JreHttpClientComponent extends AbstractLifecycle {

    private final ClientConfig clientConfig;

    private ExecutorService executorService;

    private HttpClient httpClient;

    public JreHttpClientComponent(final ClientConfig clientConfig) {
        super();

        this.clientConfig = checkNotNull(clientConfig, "ClientConfig");
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        checkNotNull(clientConfig, "ClientConfig");
        checkValue(clientConfig.getThreadPoolCoreSize(), value -> value <= 0 ? "ThreadPoolCoreSize has invalid range: " + value : null);
        checkValue(clientConfig.getThreadPoolMaxSize(), value -> value <= 0 ? "ThreadPoolMaxSize has invalid range: " + value : null);
        checkNotNull(clientConfig.getThreadNamePattern(), "ThreadNamePattern");

        int threadPoolCoreSize = clientConfig.getThreadPoolCoreSize();
        int threadPoolMaxSize = clientConfig.getThreadPoolMaxSize();
        String threadNamePattern = clientConfig.getThreadNamePattern();

        this.executorService = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new MavenProxyThreadFactory(threadNamePattern));

        // @formatter:off
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NEVER)
                .proxy(ProxySelector.getDefault())
                .connectTimeout(Duration.ofSeconds(3))
                .executor(this.executorService)
                ;
        // @formatter:on
        // .authenticator(Authenticator.getDefault())
        // .cookieHandler(CookieHandler.getDefault())
        // .sslContext(SSLContext.getDefault())
        // .sslParameters(new SSLParameters())

        this.httpClient = builder.build();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        this.httpClient = null;

        ProxyUtils.shutdown(this.executorService, getLogger());
    }
}
