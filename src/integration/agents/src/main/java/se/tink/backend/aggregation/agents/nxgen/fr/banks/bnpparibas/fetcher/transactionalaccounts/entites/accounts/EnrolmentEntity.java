package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrolmentEntity {
    @JsonProperty("pageAccueil")
    private int homePage;

    @JsonProperty("derniereModificationProfil")
    private long lastModificationProfile;

    @JsonProperty("statut")
    private int status;

    @JsonProperty("compteFavoris")
    private AccountEntity favoriteAccount;

    public int getHomePage() {
        return homePage;
    }

    public long getLastModificationProfile() {
        return lastModificationProfile;
    }

    public int getStatus() {
        return status;
    }

    public AccountEntity getFavoriteAccount() {
        return favoriteAccount;
    }
}
