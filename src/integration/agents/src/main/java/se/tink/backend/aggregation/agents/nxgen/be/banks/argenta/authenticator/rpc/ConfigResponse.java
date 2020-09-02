package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfigResponse {

    private Map<String, String> appConfigMap;

    private Map<String, String> countryMapNl;

    private Map<String, String> countryMapFr;

    private Map<String, Boolean> featureToggles;

    private List<String> onlineAppointmentBranches;

    private Boolean unavailability;

    public Map<String, String> getAppConfigMap() {
        return appConfigMap;
    }

    public Map<String, String> getCountryMapNl() {
        return countryMapNl;
    }

    public Map<String, String> getCountryMapFr() {
        return countryMapFr;
    }

    public Map<String, Boolean> getFeatureToggles() {
        return featureToggles;
    }

    public List<String> getOnlineAppointmentBranches() {
        return onlineAppointmentBranches;
    }

    public Boolean getUnavailability() {
        return unavailability;
    }
}
