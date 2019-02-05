package se.tink.backend.aggregation.storage.database.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "client_configurations")
public class ClientConfiguration {
    @Id
    private String clientName;
    @Type(type = "text")
    private String apiClientKey;
    @Type(type = "text")
    private String clusterId;
    @Type(type = "text")
    private String aggregatorId;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getApiClientKey() {
        return apiClientKey;
    }

    public void setApiClientKey(String apiClientKey) {
        this.apiClientKey = apiClientKey;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }
}
