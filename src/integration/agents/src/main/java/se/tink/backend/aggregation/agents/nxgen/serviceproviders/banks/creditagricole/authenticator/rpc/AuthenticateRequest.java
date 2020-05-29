package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class AuthenticateRequest extends DefaultAuthRequest {

    private String accountCode;
    private String llToken;
    private String userId;
    private String grantType;
    private String accountNumber;

    private AuthenticateRequest(
            String scope,
            String accountCode,
            String llToken,
            String userId,
            String grantType,
            String accountNumber) {
        super(scope);
        this.accountCode = accountCode;
        this.llToken = llToken;
        this.userId = userId;
        this.grantType = grantType;
        this.accountNumber = accountNumber;
    }

    public static AuthenticateRequest createPrimaryAuthRequest(
            String accountCode, String userId, String accountNumber) {
        return new AuthenticateRequest(
                "primary_auth", accountCode, null, userId, "password", accountNumber);
    }

    public static AuthenticateRequest createPasswordLoginRequest(
            String accountCode, String userId, String accountNumber) {
        return new AuthenticateRequest(
                "login", accountCode, null, userId, "password", accountNumber);
    }

    public static AuthenticateRequest createTokenLoginRequest(String llToken, String userId) {
        return new AuthenticateRequest("login", null, llToken, userId, "lltoken", null);
    }
}
