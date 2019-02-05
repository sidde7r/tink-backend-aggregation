package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginID {

    private String device = IngConstants.Default.MOBILE_PHONE;
    private String birthday;
    private final LoginDocument loginDocument;

    private LoginID(String username, String birthday, int usernameType) {
        loginDocument = LoginDocument.create(username, usernameType);
        this.birthday = getFormattedBirthday(birthday);
    }

    public static LoginID create(String username, String birthday, int usernameType) {
        return new LoginID(username, birthday, usernameType);
    }

    @JsonIgnore
    private String getFormattedBirthday(String birthday) {
        return new StringBuilder(birthday).insert(4, "/").insert(2, "/").toString();
    }
}
