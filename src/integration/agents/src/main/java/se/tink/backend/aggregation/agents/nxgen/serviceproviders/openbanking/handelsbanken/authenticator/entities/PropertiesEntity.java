package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.App;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.rpc.Product;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {

    private App app;

    private Product product;

    @JsonProperty("clientId")
    private ClientIdEntity clientIdEntity;

    @JsonProperty("name")
    private NameEntity nameEntity;

    public PropertiesEntity(NameEntity name) {
        this.nameEntity = name;
    }

    public PropertiesEntity(App app, ClientIdEntity clientId, Product product) {
        this.app = app;
        this.clientIdEntity = clientId;
        this.product = product;
    }

    public App getApp() {
        return app;
    }

    public Product getProduct() {
        return product;
    }

    public ClientIdEntity getClientIdEntity() {
        return clientIdEntity;
    }

    public NameEntity getNameEntity() {
        return nameEntity;
    }
}
