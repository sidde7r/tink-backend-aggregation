package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Form;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class StrongAuthenticationForm extends AbstractForm {

    public StrongAuthenticationForm setUserAccountCode(String userAccountCode) {
        this.put(Form.USER_ACCOUNT_CODE, userAccountCode);
        return this;
    }

    public StrongAuthenticationForm setUserAccountNumber(String userAccountNumber) {
        this.put(Form.USER_ACCOUNT_NUMBER, userAccountNumber);
        return this;
    }

    public StrongAuthenticationForm setLogin(String login) {
        this.put(Form.LOGIN_EMAIL, login);
        return this;
    }
}
