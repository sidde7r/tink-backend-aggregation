package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountEntity {
    private String devise;
    private String eligibleVersBenef;

    @JsonProperty("familleCompte")
    private String familyAccount;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("ibanCrypte")
    private String ibanKey;

    @JsonProperty("indicComptePro")
    private boolean indicProAccount;

    @JsonProperty("indicMineur")
    private boolean minorIndic;

    @JsonProperty("indicTitulaireCollectif")
    private boolean indicCollectiveHolder;

    @JsonProperty("indicVPP")
    private int indicvpp;

    @JsonProperty("libelleCompte")
    private String inLibel;

    @JsonProperty("nomTitulaireCompte")
    private String accountHolderName;

    @JsonProperty("numCompte")
    private String digitalAccount;

    @JsonProperty("principalCompteFavoris")
    private boolean masterAccountFavorites;

    @JsonProperty("solde")
    private double balance;

    @JsonProperty("soldeAVenir")
    private double hipComix;

    @JsonProperty("typeCompte")
    private String accountTypes;

    @JsonProperty("virementTypeChoix")
    private String transferTypeSelection;

    public String getIban() {
        return iban;
    }

    public String getIbanKey() {
        return ibanKey;
    }
}
