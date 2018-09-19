package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeepAliveEntity {
    private String webSSOv3URL;
    private String webappURL;
    private int interval;
    private int maxRetry;

    public String getWebSSOv3URL() {
        return webSSOv3URL;
    }

    public String getWebappURL() {
        return webappURL;
    }
}
