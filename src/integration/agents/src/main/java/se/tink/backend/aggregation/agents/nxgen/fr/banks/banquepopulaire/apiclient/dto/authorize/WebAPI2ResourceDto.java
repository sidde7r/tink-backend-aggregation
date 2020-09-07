package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.dto.authorize;

import java.util.Map;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class WebAPI2ResourceDto {

    private String authBusinessContextRoot;

    private String authAccessTokenURL;

    private Map<String, String> entryPoints;
}
