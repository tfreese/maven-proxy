// Created: 24.07.23
package de.freese.maven.proxy.core.component;

import java.io.Closeable;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import de.freese.maven.proxy.config.StoreConfig;
import de.freese.maven.proxy.core.lifecycle.AbstractLifecycle;

/**
 * @author Thomas Freese
 */
public class DatasourceComponent extends AbstractLifecycle {

    private final String poolName;

    private final StoreConfig storeConfig;

    private DataSource dataSource;

    public DatasourceComponent(final StoreConfig storeConfig, String poolName) {
        super();

        this.storeConfig = checkNotNull(storeConfig, "StoreConfig");
        this.poolName = checkNotNull(poolName, "PoolName");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [");
        sb.append("url=").append(storeConfig.getUrl());
        sb.append(']');

        return sb.toString();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        checkNotNull(storeConfig, "StoreConfig");
        checkNotNull(storeConfig.getDriverClassName(), "DriverClassName");
        checkNotNull(storeConfig.getUrl(), "Url");
        checkValue(storeConfig.getPoolCoreSize(), value -> value <= 0 ? "PoolCoreSize has invalid range: " + value : null);
        checkValue(storeConfig.getPoolMaxSize(), value -> value <= 0 ? "PoolMaxSize has invalid range: " + value : null);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(storeConfig.getDriverClassName());
        config.setJdbcUrl(storeConfig.getUrl());
        config.setUsername(storeConfig.getUser());
        config.setPassword(storeConfig.getPassword());
        config.setMinimumIdle(storeConfig.getPoolCoreSize());
        config.setMaximumPoolSize(storeConfig.getPoolMaxSize());
        config.setPoolName(poolName);
        config.setAutoCommit(false);

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        //        try (Connection connection = dataSource.getConnection()) {
        //            DatabaseMetaData metaData = connection.getMetaData();
        //
        //            if ("h2".equalsIgnoreCase(metaData.getDatabaseProductName())) {
        //                try (Statement statement = connection.createStatement()) {
        //                    getLogger().info("Execute shutdown command for Database 'h2'");
        //                    statement.execute("SHUTDOWN");
        //                }
        //            }
        //        }

        if (dataSource instanceof AutoCloseable ac) {
            ac.close();
        }
        else if (dataSource instanceof Closeable c) {
            c.close();
        }
    }
}
