package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {
    @JsonProperty("civilite")
    private int civilState;

    @JsonProperty("dateNaissance")
    private String dateOfBirth;

    @JsonProperty("ikpiPersonne")
    private String personIkpi;

    @JsonProperty("ikpiPersonnePhysique")
    private String physicalPersonIkpi;

    @JsonProperty("indicMineur")
    private boolean indicMinor;

    @JsonProperty("indicTitulaireCollectif")
    private boolean indicativeCollectiveHolder;

    @JsonProperty("nomComplet")
    private String fullName;

    @JsonProperty("prenom")
    private String firstName;

    public int getCivilState() {
        return civilState;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPersonIkpi() {
        return personIkpi;
    }

    public String getPhysicalPersonIkpi() {
        return physicalPersonIkpi;
    }

    public boolean isIndicMinor() {
        return indicMinor;
    }

    public boolean isIndicativeCollectiveHolder() {
        return indicativeCollectiveHolder;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }
}
