package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity.App;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.rpc.Product;

@Generated("com.robohorse.robopojogenerator")
public class Properties {

    @JsonProperty("app")
    private App app;

    @JsonProperty("product")
    private Product product;

    @JsonProperty("clientId")
    private ClientId clientId;

    @JsonProperty("name")
    private Name name;

    public Properties(Name name) {
        this.name = name;
    }

    public Properties(App app, ClientId clientId, Product product) {
        this.app = app;
        this.clientId = clientId;
        this.product = product;
    }

    public App getApp() {
        return app;
    }

    public Product getProduct() {
        return product;
    }

    public ClientId getClientId() {
        return clientId;
    }

    public Name getName() {
        return name;
    }
}
