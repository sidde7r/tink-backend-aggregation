package se.tink.backend.aggregation.configurations.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "client_configurations")
public class ClientConfiguration {
    @Id
    private String clientId;
    @Type(type = "text")
    private String clusterId;
    @Type(type = "text")
    private String cryptoId;
    @Type(type = "text")
    private String aggregatorId;

    public ClientConfiguration() {
        // Ok.
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getCryptoId() {
        return cryptoId;
    }

    public void setCryptoId(String cryptoId) {
        this.cryptoId = cryptoId;
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }
}
