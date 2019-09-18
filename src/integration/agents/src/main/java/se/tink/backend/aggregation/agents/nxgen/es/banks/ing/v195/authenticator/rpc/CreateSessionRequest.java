package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.entity.LoginDocument;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionRequest {

    @JsonProperty("device")
    private String device = IngConstants.Default.MOBILE_PHONE;

    @JsonProperty("loginDocument")
    private LoginDocument loginDocument;

    @JsonProperty("birthday")
    private LocalDate birthday;

    @JsonProperty("deviceId")
    private String deviceId;

    private CreateSessionRequest(LoginDocument loginDocument, LocalDate birthday, String deviceId) {
        this.loginDocument = loginDocument;
        this.birthday = birthday;
        this.deviceId = deviceId;
    }

    public static CreateSessionRequest create(
            String username, int usernameType, LocalDate birthday, String deviceId) {
        return new CreateSessionRequest(
                LoginDocument.create(username, usernameType), birthday, deviceId);
    }

    public String getBirthday() {
        return IngUtils.DATE_FORMATTER.format(birthday);
    }
}
