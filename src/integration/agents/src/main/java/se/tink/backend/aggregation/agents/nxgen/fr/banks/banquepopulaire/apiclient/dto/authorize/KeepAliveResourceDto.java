package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class KeepAliveResourceDto {

    private String webSSOv3URL;

    private String webappURL;

    private int interval;

    private int maxRetry;
}
