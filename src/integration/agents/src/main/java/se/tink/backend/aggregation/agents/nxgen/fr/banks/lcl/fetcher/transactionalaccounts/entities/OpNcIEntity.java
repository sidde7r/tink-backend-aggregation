package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpNcIEntity {
    private int totalDebitOperations;
    private int totalCreditOperations;

    @JsonProperty("signeSoldeOperations")
    private String signBalanceOperations;

    @JsonProperty("valeurSoldeOperations")
    private int valueBalanceOperations;

    private String tri;
    private String signeMntTotalCpt;
    private int mntTotalCpt;
    private String mntTotalCptFormat;
    private int nbMvtCpt;

    public int getTotalDebitOperations() {
        return totalDebitOperations;
    }

    public int getTotalCreditOperations() {
        return totalCreditOperations;
    }

    public String getSignBalanceOperations() {
        return signBalanceOperations;
    }

    public int getValueBalanceOperations() {
        return valueBalanceOperations;
    }

    public String getTri() {
        return tri;
    }

    public String getSigneMntTotalCpt() {
        return signeMntTotalCpt;
    }

    public int getMntTotalCpt() {
        return mntTotalCpt;
    }

    public String getMntTotalCptFormat() {
        return mntTotalCptFormat;
    }

    public int getNbMvtCpt() {
        return nbMvtCpt;
    }
}
