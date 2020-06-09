package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.entity.LoginDocument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreateSessionRequest {

    @JsonProperty private String credentialsToken;

    @JsonProperty private String device = IngConstants.Default.MOBILE_PHONE;

    @JsonProperty private LoginDocument loginDocument;

    @JsonProperty private LocalDate birthday;

    @JsonProperty private String deviceId;

    public static CreateSessionRequest fromUsername(
            String username, int usernameType, LocalDate birthday, String deviceId) {
        CreateSessionRequest request = new CreateSessionRequest();
        request.loginDocument = LoginDocument.create(username, usernameType);
        request.birthday = birthday;
        request.deviceId = deviceId;
        return request;
    }

    public static CreateSessionRequest fromCredentialsToken(
            String credentialsToken, String deviceId) {
        CreateSessionRequest request = new CreateSessionRequest();
        request.credentialsToken = credentialsToken;
        request.deviceId = deviceId;
        return request;
    }

    public String getBirthday() {
        return birthday == null ? null : IngUtils.DATE_FORMATTER.format(birthday);
    }
}
