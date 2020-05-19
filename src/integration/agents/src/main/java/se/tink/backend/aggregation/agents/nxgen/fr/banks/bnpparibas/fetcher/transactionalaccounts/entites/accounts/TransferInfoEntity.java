package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferInfoEntity {
    @JsonProperty("accesInter")
    private String interAccess;

    @JsonProperty("accesSepa")
    private String sepaAccess;

    private String civilite;
    private int cslModification;
    private int cslProduction;

    @JsonProperty("dateDuJour")
    private String todaySDate;

    private String dateMax;

    @JsonProperty("indicBeneficiaire")
    private boolean indicatesBeneficiaire;

    private boolean indicPro;
    private boolean indicSelfcare;
    //    @JsonProperty("listeBeneficiaires")
    //    private List<BeneficiariesListEntity> beneficiariesList;
    @JsonProperty("listeComptesCrediteur")
    private List<TransactionAccountEntity> creditAccountsList;
    //    @JsonProperty("listeComptesDebiteur")
    //    private List<TransactionAccountEntity> debtorAccountsList;
    @JsonProperty("nom")
    private String lastName;

    @JsonProperty("prenom")
    private String firstName;

    @JsonProperty("urlAiguillage")
    private String urlRouting;

    public List<TransactionAccountEntity> getCreditAccountsList() {
        return creditAccountsList;
    }
}
