package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsInfoEntity {
    private InfoUdcEntity infoUdc;
    @JsonProperty("dateDuJour")
    private String todaySDate;
    private String pagination;
    @JsonProperty("urlAiguillage")
    private String urlReferrals;
    private int cslProduction;
    private int cslModification;
    @JsonProperty("compte")
    private AccountTransactionsEntity accountTransactions;

    public InfoUdcEntity getInfoUdc() {
        return infoUdc;
    }

    public String getTodaySDate() {
        return todaySDate;
    }

    public String getPagination() {
        return pagination;
    }

    public String getUrlReferrals() {
        return urlReferrals;
    }

    public int getCslProduction() {
        return cslProduction;
    }

    public int getCslModification() {
        return cslModification;
    }

    public AccountTransactionsEntity getAccountTransactions() {
        return accountTransactions;
    }
}
