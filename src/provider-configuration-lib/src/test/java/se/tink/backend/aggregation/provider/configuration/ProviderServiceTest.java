package se.tink.backend.aggregation.provider.configuration;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.http.resources.ProviderServiceResource;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderServiceTest {
    private static Injector injector;
    private static final ImmutableMap<String, ClusterId> CLUSTERS = ImmutableMap.<String, ClusterId>builder()
            .put("cornwall-testing", ClusterId.of("cornwall", "testing"))
            .put("cornwall-production", ClusterId.of("cornwall", "production"))
            .put("farnham-staging", ClusterId.of("farnham", "staging"))
            .put("farnham-production", ClusterId.of("farnham", "production"))
            .put("kirkby-staging", ClusterId.of("kirkby", "staging"))
            .put("kirkby-production", ClusterId.of("kirkby", "production"))
            .put("leeds-staging", ClusterId.of("leeds", "staging"))
            .put("leeds-production", ClusterId.of("leeds", "production"))
            .put("neston-staging", ClusterId.of("neston", "staging"))
            .put("neston-preprod", ClusterId.of("neston", "preprod"))
            .put("neston-production", ClusterId.of("neston", "production"))
            .put("newport-staging", ClusterId.of("newport", "staging"))
            .put("newport-production", ClusterId.of("newport", "production"))
            .put("oxford-staging", ClusterId.of("oxford", "staging"))
            .put("oxford-production", ClusterId.of("oxford", "production"))
            .put("no-available-providers", ClusterId.of("empty", "empty"))
            .put("empty_clusterid", ClusterId.createEmpty())
            .build();
    private ProviderServiceResource resource;

    @BeforeClass
    public static void init() {
        injector = InjectorFactory.get("etc/development-provider-configuration-server.yml");
    }

    @Before
    public void setup() {
        resource = injector.getInstance(ProviderServiceResource.class);
    }

    @Test
    public void whenClusterDoNotHaveAvailableProviders_returnEmptyList() {
        List<ProviderConfigurationDTO> emptyList = resource.list(
                "en", ClusterInfo.createForTesting(CLUSTERS.get("no-available-providers")));

        assertThat(emptyList).isEmpty();
    }

    @Test(expected = WebApplicationException.class)
    public void whenInvalidClusterId_expectedWebApplicationException() {
        List<ProviderConfigurationDTO> en = resource
                .list("en", ClusterInfo.createForTesting(CLUSTERS.get("empty-clusterid")));
    }
}
