package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HolderEntity {
    @JsonProperty("nom")
    private String lastName;

    @JsonProperty("indicMineur")
    private String minorIndic;

    @JsonProperty("prenom")
    private String firstName;

    private String civilite;
    private int idCivilite;

    @JsonProperty("indicTitulaireCollectif")
    private String indicCollectiveHolder;

    private String ikpi;

    @JsonProperty("dateDeNaissance")
    private String dateOfBirth;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
