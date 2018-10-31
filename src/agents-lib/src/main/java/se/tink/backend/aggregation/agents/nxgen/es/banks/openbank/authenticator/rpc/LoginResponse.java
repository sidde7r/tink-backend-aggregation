package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private String application;
    private String tokenCredential;

    public static LoginResponse empty() {
        return new LoginResponse();
    }

    public String getApplication() {
        return application;
    }

    public boolean hasTokenCredential() {
        return Objects.nonNull(tokenCredential) && !Strings.isNullOrEmpty(tokenCredential);
    }

    public String getTokenCredential() {
        return tokenCredential;
    }
}
