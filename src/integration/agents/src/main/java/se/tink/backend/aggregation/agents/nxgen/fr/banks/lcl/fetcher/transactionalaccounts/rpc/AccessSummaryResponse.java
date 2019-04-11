package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountGroupEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessSummaryResponse {
    @JsonProperty("situationGlobale")
    private String totalSummary;

    private String clientApplication;
    private String typePers;
    private boolean errorPerso;

    @JsonProperty("situationEpargne")
    private String savingsSummary;

    private boolean isOpNCBlooped;
    private boolean persoIsActive;

    @JsonProperty("clientAffichageSite")
    private String clientDisplaySite;

    @JsonProperty("famille")
    private List<AccountGroupEntity> accountGroupList;

    @JsonProperty("clientIsBanquePrivee")
    private boolean clientIsPrivateBanking;

    @JsonProperty("TimeoutPerso")
    private String timeoutPerso;

    private String clientApplicationOrigine;

    @JsonProperty("libellePers")
    private String ownerName;

    @JsonProperty("situationHorsEpargne")
    private String summaryExclSavings;

    public String getTotalSummary() {
        return totalSummary;
    }

    public String getClientApplication() {
        return clientApplication;
    }

    public String getTypePers() {
        return typePers;
    }

    public boolean isErrorPerso() {
        return errorPerso;
    }

    public String getSavingsSummary() {
        return savingsSummary;
    }

    public boolean isOpNCBlooped() {
        return isOpNCBlooped;
    }

    public boolean isPersoIsActive() {
        return persoIsActive;
    }

    public String getClientDisplaySite() {
        return clientDisplaySite;
    }

    public List<AccountGroupEntity> getAccountGroupList() {
        return accountGroupList;
    }

    public boolean isClientIsPrivateBanking() {
        return clientIsPrivateBanking;
    }

    public String getTimeoutPerso() {
        return timeoutPerso;
    }

    public String getClientApplicationOrigine() {
        return clientApplicationOrigine;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getSummaryExclSavings() {
        return summaryExclSavings;
    }
}
