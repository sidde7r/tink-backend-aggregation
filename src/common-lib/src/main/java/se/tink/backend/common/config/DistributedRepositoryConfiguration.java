package se.tink.backend.common.config;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.cassandra.config.CompressionType;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext;
import org.springframework.data.cassandra.mapping.CassandraMappingContext;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = {
        "se.tink.backend.common.repository.cassandra",
        "se.tink.backend.consent.repository.cassandra",
        "se.tink.backend.sms.otp.repository.cassandra"
})
public class DistributedRepositoryConfiguration {

    private static AtomicReference<DistributedDatabaseConfiguration> configuration = new AtomicReference<>();

    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();

    public static CreateKeyspaceSpecification keyspaceSpec;

    public static void setConfiguration(DistributedDatabaseConfiguration configuration) {
        DistributedRepositoryConfiguration.configuration.set(configuration);

        String keyspace = configuration.getKeyspace();
        keyspaceSpec = CreateKeyspaceSpecification.createKeyspace(keyspace).ifNotExists().withSimpleReplication();
    }

    private static String constructSeedsString(List<String> seeds) {
        String result = COMMA_JOINER.join(seeds);
        Preconditions.checkArgument(!result.contains(":"), "Port must not be specified to cassandra hosts list.");
        return result;
    }

    public DistributedRepositoryConfiguration() {
        Preconditions.checkState(configuration.get() != null, "configuration is not set");
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {

        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setContactPoints(constructSeedsString(configuration.get().getSeeds()));
        cluster.setPort(configuration.get().getPort());
        cluster.setCompressionType(CompressionType.SNAPPY);
        cluster.setLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder()
                .withLocalDc(configuration.get().getDatacenter())
                .withUsedHostsPerRemoteDc(configuration.get().getRemoteFallbackServers())
                .build()));

        if (configuration.get().getCreateKeyspaceOnStart()) {
            // This is done to make the life of a new developer easier.
            cluster.setKeyspaceCreations(Lists.newArrayList(keyspaceSpec));
        }

        // MAYBE: Enable SSL

        if (configuration.get().getUsername() != null && configuration.get().getPassword() != null) {
            final AuthProvider loginAuthProvider = new PlainTextAuthProvider(configuration.get().getUsername(),
                    configuration.get().getPassword());
            cluster.setAuthProvider(loginAuthProvider);
        }

        return cluster;
    }

    @Bean
    public CassandraMappingContext mappingContext() {
      return new BasicCassandraMappingContext();
    }

    @Bean
    public CassandraConverter converter() {
        return new MappingCassandraConverter(mappingContext());
    }

    @Bean
    public CassandraSessionFactoryBean session() throws Exception {

        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
        session.setCluster(cluster().getObject());
        session.setKeyspaceName(configuration.get().getKeyspace());
        session.setConverter(converter());
        session.setSchemaAction(SchemaAction.NONE);

        return session;
    }

    @Bean
    public CassandraOperations cassandraTemplate() throws Exception {
      return new CassandraTemplate(session().getObject());
    }

}
