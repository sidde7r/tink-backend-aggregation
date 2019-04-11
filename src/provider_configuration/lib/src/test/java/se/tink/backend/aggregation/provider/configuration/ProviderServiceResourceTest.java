package se.tink.backend.aggregation.provider.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterId;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.http.resources.ProviderServiceResource;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

public class ProviderServiceResourceTest extends ProviderConfigurationServiceTestBase {

    private static final ImmutableMap<String, ClusterId> CLUSTERS =
            ImmutableMap.<String, ClusterId>builder()
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

    @Before
    public void setup() {
        resource = injector.getInstance(ProviderServiceResource.class);
    }

    @Test
    public void whenClusterDoNotHaveAvailableProviders_returnEmptyList() {
        List<ProviderConfigurationDTO> emptyList =
                resource.list(ClusterInfo.of(CLUSTERS.get("no-available-providers")));

        assertThat(emptyList).isEmpty();
    }
}
