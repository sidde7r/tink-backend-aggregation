package se.tink.backend.common.config.repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA repository configurator for aggregation instances.
 * <p>
 * Don't forget to call
 * {@link SingletonRepositoryConfiguration#setConfiguration(se.tink.backend.common.config.DatabaseConfiguration)} before
 * using this class!
 * </p>
 */
@Configuration
@EnableJpaRepositories(basePackages = {
        "se.tink.backend.common.repository.mysql.aggregation",
        "se.tink.backend.aggregation.configurations.repositories"
})
@EnableTransactionManagement
class AggregationRepositoryConfiguration implements RepositoryConfigurator {
    private SingletonRepositoryConfiguration realConfigurator;

    public AggregationRepositoryConfiguration() {
        realConfigurator = new SingletonRepositoryConfiguration();
    }

    @Bean
    @Override
    public DataSource dataSource() {
        return realConfigurator.dataSource();
    }

    @Bean
    @Override
    public EntityManagerFactory entityManagerFactory() {
        return realConfigurator.entityManagerFactory();
    }

    @Bean
    @Override
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return realConfigurator.hibernateExceptionTranslator();
    }

    @Bean
    @Override
    public PlatformTransactionManager transactionManager() {
        return realConfigurator.transactionManager();
    }

}
