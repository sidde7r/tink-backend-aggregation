package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankAuthenticateResponse {
    @JsonProperty("etk_klo")
    private String etkKlo;
    @JsonProperty("etk_pvm")
    private String etkPvm;
    @JsonProperty("avl_jaljella")
    private String cardId;
    @JsonProperty("postituskd_avll")
    private String postituskdAvll;
    @JsonProperty("pankinLuku")
    private String userKey;
    private int httpResponseCode;
    private String errorCode;

    public int getHttpResponseCode(){
        return this.httpResponseCode;
    }

    public String getEtkKlo() {
        return etkKlo;
    }

    public String getEtkPvm() {
        return etkPvm;
    }

    public String getCardId() {
        return cardId;
    }

    public String getPostituskdAvll() {
        return postituskdAvll;
    }

    public String getUserKey() {
        return userKey;
    }

    public boolean isUnauthenticated() {
        return httpResponseCode == OpBankConstants.Authentication.UNAUTHENTICATED_STATUS;
    }
}
