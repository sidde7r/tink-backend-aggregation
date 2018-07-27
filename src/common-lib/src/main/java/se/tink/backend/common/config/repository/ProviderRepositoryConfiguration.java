package se.tink.backend.common.config.repository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = {
        "se.tink.backend.aggregation.provider.configuration.repositories.mysql",
        "se.tink.backend.common.repository.mysql.aggregation.repositories"
})
@EnableTransactionManagement
public class ProviderRepositoryConfiguration  implements RepositoryConfigurator {
    private SingletonRepositoryConfiguration realConfigurator;

    public ProviderRepositoryConfiguration() {
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
