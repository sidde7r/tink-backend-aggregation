package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConnectedHolderEntity {
    @JsonProperty("civilite")
    private String civilState;
    @JsonProperty("idCivilite")
    private int civilStateId;
    private String ikpi;
    @JsonProperty("nomComplet")
    private String fullName;
    @JsonProperty("derniereConnexion")
    private String lastConnection;

    public String getCivilState() {
        return civilState;
    }

    public int getCivilStateId() {
        return civilStateId;
    }

    public String getIkpi() {
        return ikpi;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLastConnection() {
        return lastConnection;
    }
}
