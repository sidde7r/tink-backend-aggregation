package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.AppEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.liveenrolement.entity.SubscriptionProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PropertiesEntity {

    private AppEntity app;

    private SubscriptionProductEntity product;

    @JsonProperty("clientId")
    private ClientIdEntity clientIdEntity;

    @JsonProperty("name")
    private NameEntity nameEntity;

    public PropertiesEntity(NameEntity name) {
        this.nameEntity = name;
    }

    public PropertiesEntity(AppEntity app, ClientIdEntity clientId, SubscriptionProductEntity product) {
        this.app = app;
        this.clientIdEntity = clientId;
        this.product = product;
    }

    public AppEntity getApp() {
        return app;
    }

    public SubscriptionProductEntity getProduct() {
        return product;
    }

    public ClientIdEntity getClientIdEntity() {
        return clientIdEntity;
    }

    public NameEntity getNameEntity() {
        return nameEntity;
    }
}
