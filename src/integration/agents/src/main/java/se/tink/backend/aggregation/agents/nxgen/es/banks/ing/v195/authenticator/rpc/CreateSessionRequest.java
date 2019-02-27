package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.entity.LoginDocument;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.time.LocalDate;

@JsonObject
public class CreateSessionRequest {

    @JsonProperty("device")
    private String device = IngConstants.Default.MOBILE_PHONE;

    @JsonProperty("loginDocument")
    private LoginDocument loginDocument;

    @JsonProperty("birthday")
    private LocalDate birthday;

    private CreateSessionRequest(LoginDocument loginDocument, LocalDate birthday) {
        this.loginDocument = loginDocument;
        this.birthday = birthday;
    }

    public static CreateSessionRequest create(String username, int usernameType, LocalDate birthday) {
        return new CreateSessionRequest(LoginDocument.create(username, usernameType), birthday);
    }

    public String getBirthday() {
        return IngUtils.DATE_FORMATTER.format(birthday);
    }
}
