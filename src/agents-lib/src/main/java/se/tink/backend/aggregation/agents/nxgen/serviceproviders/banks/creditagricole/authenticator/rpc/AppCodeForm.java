package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Form;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AppCodeForm extends AbstractForm {

    public AppCodeForm() {
    }

    public AppCodeForm setUserAccountNumber(String userAccountNumber) {
        this.put(Form.USER_ACCOUNT_NUMBER, userAccountNumber);
        return this;
    }

    public AppCodeForm setUserAccountCode(String userAccountCode) {
        this.put(Form.USER_ACCOUNT_CODE, userAccountCode);
        return this;
    }

    public AppCodeForm setAppCode(String appCode) {
        this.put(Form.APP_CODE, appCode);
        return this;
    }
}
