package se.tink.libraries.repository.config.repository;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import se.tink.libraries.repository.config.DatabaseConfiguration;

/** Does the heavy lifting for {@link AggregationRepositoryConfiguration}. */
public class SingletonRepositoryConfiguration {
    private static DatabaseConfiguration configuration;

    static DatabaseConfiguration getConfiguration() {
        return configuration;
    }

    public static void setConfiguration(DatabaseConfiguration configuration) {
        SingletonRepositoryConfiguration.configuration = configuration;
    }

    // Using a memorizing supplier here to make sure that the transaction manager is using the same
    // entityManagerFactory
    // as #entityManagerFactory().
    private Supplier<EntityManagerFactory> entityManagerFactorySupplier =
            Suppliers.memoize(
                    new Supplier<EntityManagerFactory>() {

                        @Override
                        public EntityManagerFactory get() {
                            HibernateJpaVendorAdapter vendorAdapter =
                                    new HibernateJpaVendorAdapter();

                            vendorAdapter.setGenerateDdl(configuration.generateDdl());
                            vendorAdapter.setShowSql(configuration.isShowSql());

                            LocalContainerEntityManagerFactoryBean factory =
                                    new LocalContainerEntityManagerFactoryBean();

                            factory.setPersistenceUnitName(configuration.getPersistenceUnitName());
                            factory.setJpaVendorAdapter(vendorAdapter);
                            factory.setDataSource(dataSource());
                            factory.afterPropertiesSet();

                            return factory.getObject();
                        }
                    });

    public SingletonRepositoryConfiguration() {
        Preconditions.checkState(
                configuration != null,
                "Must call SingletonRepositoryConfiguration#setConfiguration(...) before instantiating SingletonRepositoryConfiguration.");
    }

    DataSource dataSource() {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();

        try {
            dataSource.setDriverClass(configuration.getDriverClass());
            dataSource.setJdbcUrl(configuration.getUrl());
            dataSource.setUser(configuration.getUsername());
            dataSource.setPassword(configuration.getPassword());

            // Parameters to make sure connections don't die.
            // See http://hibernatedb.blogspot.se/2009/05/c3p0properties.html for a (great!) list of
            // default value for
            // dataSource.

            dataSource.setPreferredTestQuery("SELECT 1");

            dataSource.setIdleConnectionTestPeriod((int) TimeUnit.MINUTES.toSeconds(5));
            dataSource.setMaxIdleTime((int) TimeUnit.HOURS.toSeconds(2));

            // More gracefully handle AWS RDS instance failovers. Apart from this, we also want to
            // make sure that
            // `connectTimeout` and `socketTimeout` is set on the JDBC
            // URL. See [1].
            //
            // [1]
            // https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-configuration-properties.html
            dataSource.setTestConnectionOnCheckout(true);
            if (configuration.getAcquireRetryAttemptsSeconds() >= 0) {
                dataSource.setAcquireRetryAttempts(configuration.getAcquireRetryAttemptsSeconds());
            }
            if (configuration.getAcquireRetryDelaySeconds() >= 0) {
                dataSource.setAcquireRetryDelay(configuration.getAcquireRetryDelaySeconds());
            }
            dataSource.setTestConnectionOnCheckout(configuration.getTestConnectionOnCheckout());
            dataSource.setMaxPoolSize(configuration.getMaxPoolSize());

            return dataSource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    EntityManagerFactory entityManagerFactory() {
        // Using a memorizing supplier here to always return the same factory. Without this,
        // #transactionManager() will
        // build an incorrectly associated transaction manager that will not work with the entity
        // manager returned here.
        // This will in itself make JPA throw {@code TransactionRequiredException}s.
        return entityManagerFactorySupplier.get();
    }

    HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    PlatformTransactionManager transactionManager() {
        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory());
        return txManager;
    }
}
