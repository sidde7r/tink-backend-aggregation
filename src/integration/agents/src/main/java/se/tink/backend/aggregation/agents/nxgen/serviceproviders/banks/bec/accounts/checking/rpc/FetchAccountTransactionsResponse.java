package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.RecordEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountTransactionsResponse {

    private Boolean browsePossible;
    private String nextPageId;
    private Double availableBalance;
    private String availableBalanceTxt;
    private Double grantedOverdraft;
    private String grantedOverdraftTxt;
    private String accountHolder;
    private Boolean hasCards;
    private Boolean ignoreAvailableBalance;
    private Double balance;
    private String balanceTxt;
    private String urlMatch;
    private List<RecordEntity> record;

    public Boolean getBrowsePossible() {
        return browsePossible;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public String getAvailableBalanceTxt() {
        return availableBalanceTxt;
    }

    public Double getGrantedOverdraft() {
        return grantedOverdraft;
    }

    public String getGrantedOverdraftTxt() {
        return grantedOverdraftTxt;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public Boolean getHasCards() {
        return hasCards;
    }

    public Boolean getIgnoreAvailableBalance() {
        return ignoreAvailableBalance;
    }

    public Double getBalance() {
        return balance;
    }

    public String getBalanceTxt() {
        return balanceTxt;
    }

    public String getUrlMatch() {
        return urlMatch;
    }

    public List<RecordEntity> getRecord() {
        return record;
    }
}
