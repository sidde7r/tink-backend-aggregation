package se.tink.backend.common.config.repository;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configures a JPAInstanceBuilderFactoryFactoryBuilder. *joke*
 * <p>
 * On a more serious note, this interface exists to make sure that {@link MainRepositoryConfiguration} and
 * {@link AggregationRepositoryConfiguration} implement the same bean factories.
 * </p>
 */
interface RepositoryConfigurator {
    DataSource dataSource();

    EntityManagerFactory entityManagerFactory();

    HibernateExceptionTranslator hibernateExceptionTranslator();

    PlatformTransactionManager transactionManager();
}
