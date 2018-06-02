package se.tink.backend.core;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;

public class ClusterProviderId implements Serializable {
    @NotNull
    @Column(name = "clusterid")
    private String clusterId;
    @NotNull
    @Column(name = "providername")
    private String providerName;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClusterProviderId that = (ClusterProviderId) o;
        return Objects.equals(clusterId, that.clusterId) &&
                Objects.equals(providerName, that.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, providerName);
    }
}
