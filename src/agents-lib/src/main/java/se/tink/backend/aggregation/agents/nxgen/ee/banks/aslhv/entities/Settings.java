package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Settings {

    @JsonProperty("services")
    private List<ServicesItem> services;

    @JsonProperty("notifications")
    private Notifications notifications;

    public List<ServicesItem> getServices() {
        return services;
    }

    public Notifications getNotifications() {
        return notifications;
    }
}
