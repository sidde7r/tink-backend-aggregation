package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities.InfoClientEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities.TopClientEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities.AccountListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseErrorResponse {
    private TopClientEntity topClient;
    private String application;
    @JsonProperty("identifiantSite")
    private String siteId;
    @JsonProperty("urlMessagerie")
    private String urlMessaging;
    private InfoClientEntity infoClient;
    @JsonProperty("numContrat")
    private String contractNumber;
    private String date;
    @JsonProperty("comptes")
    private AccountListEntity accounts;

    public TopClientEntity getTopClient() {
        return topClient;
    }

    public String getApplication() {
        return application;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getUrlMessaging() {
        return urlMessaging;
    }

    public InfoClientEntity getInfoClient() {
        return infoClient;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getDate() {
        return date;
    }

    public AccountListEntity getAccounts() {
        return accounts;
    }
}
