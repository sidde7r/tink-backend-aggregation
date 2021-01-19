package se.tink.backend.aggregation.agents.nxgen.se.other.csn.authenticator.bankid.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class LoginForm extends AbstractForm {
    public LoginForm(String username) {
        this.put(CSNConstants.LoginKeys.METHOD, CSNConstants.LoginValues.VALIDATE_BANK_ID);
        this.put(CSNConstants.LoginKeys.SSN, username);
    }

    public LoginForm() {
        this.put(CSNConstants.LoginKeys.METHOD, CSNConstants.LoginValues.TRY_LOGIN);
        this.put(CSNConstants.LoginKeys.CSN_LOGIN, CSNConstants.LoginValues.BANK_ID);
    }
}
