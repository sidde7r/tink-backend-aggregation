package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class BankResourceDto {

    private String id;

    private String name;

    private String shortName;

    private String img;

    @JsonProperty("icone")
    private String icon;

    private String logo;

    private String anoBaseUrl;

    private String applicationAPIContextRoot;

    private String webMobileContextRoot;

    @JsonProperty("welcome-msg")
    private String welcomeMsg;
}
