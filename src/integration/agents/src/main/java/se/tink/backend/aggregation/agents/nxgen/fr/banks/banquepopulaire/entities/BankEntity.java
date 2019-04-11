package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankEntity {
    private String id;
    private String name;
    private String shortName;
    private String img;
    private String icone;
    private String logo;
    private String anoBaseUrl;
    private String applicationAPIContextRoot;
    private String webMobileContextRoot;

    @JsonProperty("welcome-msg")
    private String welcomeMsg;

    public String getId() {
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getAnoBaseUrl() {
        return anoBaseUrl;
    }

    public String getApplicationAPIContextRoot() {
        return applicationAPIContextRoot;
    }

    public String getWebMobileContextRoot() {
        return webMobileContextRoot;
    }
}
