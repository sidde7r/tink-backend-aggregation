package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationData {

    @JsonProperty("code_etoile")
    private String codeEtoile;
    @JsonProperty("date_dern_con")
    private String dateDernCon;
    @JsonProperty("canal_dern_con")
    private String canalDernCon;
    @JsonProperty("droits")
    private List<String> rights;
    @JsonProperty("clesession")
    private String sessionKey;
    @JsonProperty("id_cle")
    private String keyId;
    @JsonProperty("jeton")
    private String token;
    @JsonProperty("id_stat")
    private String idStat;
    @JsonProperty("profil_tiers")
    private ThirdPartyProfileEntity thirdPartyProfile;

    public String getSessionKey() {
        return sessionKey;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getToken() {
        return token;
    }

}
