package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc.OpBankResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankLoginResponseEntity extends OpBankResponseEntity {
    private String authMethod;

    @JsonProperty("etk_klo")
    private String time;

    @JsonProperty("etk_pvm")
    private String date;

    @JsonProperty("sukunimi_kayt")
    private String name;

    @JsonProperty("avl_jaljella")
    private String avlJaljella;

    @JsonProperty("postituskd_avll")
    private String postituskdAvll;

    public String getAuthMethod() {
        return authMethod;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getAvlJaljella() {
        return avlJaljella;
    }

    public String getPostituskdAvll() {
        return postituskdAvll;
    }

    public boolean loginSingleFactor() {
        return OpBankConstants.Authentication.Level.LOGGEDIN_WITH_LIGHTLOGIN.equalsIgnoreCase(
                authMethod);
    }

    public boolean loginTwoFactor() {
        return OpBankConstants.Authentication.Level.LOGGEDIN.equalsIgnoreCase(authMethod);
    }

    public boolean incorrectCredentials() {
        return getStatus() == 1;
    }
}
