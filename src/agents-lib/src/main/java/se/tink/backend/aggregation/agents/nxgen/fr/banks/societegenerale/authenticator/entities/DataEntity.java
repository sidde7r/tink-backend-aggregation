package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DataEntity {

    @JsonProperty("code_etoile")
    private String codeEtoile;
    @JsonProperty("date_dern_con")
    private String dateDernCon;
    @JsonProperty("canal_dern_con")
    private String canalDernCon;
    @JsonProperty("droits")
    private List<String> rights;
    private String clesession;
    @JsonProperty("id_cle")
    private String idCle;
    @JsonProperty("jeton")
    private String token;
    @JsonProperty("id_stat")
    private String idStat;
    @JsonProperty("profil_tiers")
    private ProfilTiersEntity profilTiers;

    public String getToken() {
        return token;
    }

}
