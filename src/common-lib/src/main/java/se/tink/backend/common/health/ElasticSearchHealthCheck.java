package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Objects;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthStatus;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

public class ElasticSearchHealthCheck extends HealthCheck {

    private final Client client;
    private String[] indexNames;
    
    public ElasticSearchHealthCheck(Client client) {
        this.client = client;
    }
    
    public ElasticSearchHealthCheck(Client client, String... indexNames) {
        this(client);
        this.indexNames = indexNames;
    }
    
    @Override
    protected Result check() throws Exception {
        
        final ClusterHealthRequest clusterHealthRequest = new ClusterHealthRequest()
                .timeout(TimeValue.timeValueSeconds(5))
                .waitForYellowStatus();
        
        final ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest).actionGet();

        if (clusterHealth.isTimedOut()) {
            return Result.unhealthy("Timed out");
        }

        if (Objects.equal(ClusterHealthStatus.RED, clusterHealth.getStatus())) {
            return Result.unhealthy("Critical; data is partially unavailable (RED)");
        }
        
        if (indexNames != null && indexNames.length > 0) {
            if (!indicesExist(indexNames)) {
                return Result.unhealthy("Missing indices");
            }
        }

        if (Objects.equal(ClusterHealthStatus.YELLOW, clusterHealth.getStatus())) {
            return Result.healthy("Operational, but risk for data loss (YELLOW)");
        }

        return Result.healthy();
    }
    
    private boolean indicesExist(String... indexNames) {
        return client.admin().indices().exists(new IndicesExistsRequest(indexNames)).actionGet().isExists();
    }
}
