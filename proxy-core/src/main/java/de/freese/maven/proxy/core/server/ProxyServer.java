// Created: 22.07.23
package de.freese.maven.proxy.core.server;

import de.freese.maven.proxy.config.ServerConfig;
import de.freese.maven.proxy.core.lifecycle.Lifecycle;
import de.freese.maven.proxy.core.repository.Repository;

/**
 * @author Thomas Freese
 */
public interface ProxyServer extends Lifecycle {

    ProxyServer addContextRoot(String contextRoot, Repository repository);

    ProxyServer setConfig(ServerConfig serverConfig);
}
