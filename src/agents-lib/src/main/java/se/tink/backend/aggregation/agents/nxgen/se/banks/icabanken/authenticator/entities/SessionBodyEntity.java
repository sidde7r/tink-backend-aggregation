package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SessionBodyEntity {
    @JsonProperty("Session")
    private SessionEntity session;
    @JsonProperty("Customer")
    private CustomerEntity customer;
    @JsonProperty("Settings")
    private SettingsEntity settings;
    @JsonProperty("ClosedChannels")
    private List<Object> closedChannels;
    @JsonProperty("StaticData")
    private StaticDataEntity staticData;

    public SessionEntity getSession() {
        return session;
    }

    public CustomerEntity getCustomer() {
        return customer;
    }

    public SettingsEntity getSettings() {
        return settings;
    }

    public List<Object> getClosedChannels() {
        return closedChannels;
    }

    public StaticDataEntity getStaticData() {
        return staticData;
    }
}
