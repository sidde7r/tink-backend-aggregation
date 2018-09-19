package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AppConfigEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankConfigResponse {
    private AppConfigEntity appConfig;
    private String legal;

    public AppConfigEntity getAppConfig() {
        return appConfig;
    }

    public String getLegal() {
        return legal;
    }
}
