package se.tink.backend.core;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "cluster_provider_configurations")
public class ClusterProviderConfiguration {
    @EmbeddedId
    private ClusterProviderId clusterProviderId;

    public ClusterProviderId getClusterProviderId() {
        return clusterProviderId;
    }

    public void setClusterProviderId(ClusterProviderId clusterProviderId) {
        this.clusterProviderId = clusterProviderId;
    }
}
