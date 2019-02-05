package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginInformationEntity {
    @JsonProperty("dateDerniereIdentification")
    private String dateLastLogin;
    @JsonProperty("heureDerniereIdentification")
    private String timeLastLogin;
    @JsonProperty("typeGrille")
    private String gridType;

    public String getDateLastLogin() {
        return dateLastLogin;
    }

    public String getTimeLastLogin() {
        return timeLastLogin;
    }

    public String getGridType() {
        return gridType;
    }
}
