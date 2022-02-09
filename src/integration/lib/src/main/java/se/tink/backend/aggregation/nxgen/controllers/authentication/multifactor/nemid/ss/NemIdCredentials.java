package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdPasswordField;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage.NemIdUserIdField;
import se.tink.libraries.i18n_aggregation.Catalog;

@Getter
@Builder
public class NemIdCredentials {

    private String userId;
    private String password;

    public List<Field> getFieldsToAskUserFor(Catalog catalog) {
        List<Field> fields = new ArrayList<>();
        if (userId == null) {
            fields.add(NemIdUserIdField.build(catalog));
        }
        if (password == null) {
            fields.add(NemIdPasswordField.build(catalog));
        }
        return fields;
    }

    public void setMissingCredentials(Map<String, String> supplementalInfoResponse) {
        if (userId == null) {
            userId = supplementalInfoResponse.get(NemIdUserIdField.NAME);
        }
        if (password == null) {
            password = supplementalInfoResponse.get(NemIdPasswordField.NAME);
        }
    }

    public void assertNoMissingCredentials() {
        if (userId == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception("Missing NemID userId");
        }
        if (password == null) {
            throw LoginError.INCORRECT_CREDENTIALS.exception("Missing NemID password");
        }
    }
}
