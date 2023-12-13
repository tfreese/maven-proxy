// Created: 27.07.23
package de.freese.maven.proxy.main;

import de.freese.maven.proxy.core.MavenProxyLauncher;

/**
 * @author Thomas Freese
 */
public final class ProxyMain {
    public static void main(final String[] args) throws Exception {
        MavenProxyLauncher.main(args);
    }

    private ProxyMain() {
        super();
    }
}
