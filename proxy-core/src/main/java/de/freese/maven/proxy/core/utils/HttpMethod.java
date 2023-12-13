// Created: 19.07.23
package de.freese.maven.proxy.core.utils;

/**
 * @author Thomas Freese
 */
public enum HttpMethod {
    /**
     * Exist Query
     */
    HEAD,
    /**
     * Download Binary
     */
    GET,
    /**
     * Deploy
     */
    PUT;

    public static HttpMethod get(final String method) {
        for (HttpMethod httpMethod : values()) {
            if (httpMethod.name().equalsIgnoreCase(method)) {
                return httpMethod;
            }
        }

        throw new UnsupportedOperationException("HttpMethod not supported: " + method);
    }
}
