package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Form;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class RestoreProfileForm extends AbstractForm {

    public RestoreProfileForm(String accountNumber, String accountCode, String pin) {
        this.put(Form.ACCOUNT_NUMBER, accountNumber);
        this.put(Form.ACCOUNT_CODE, accountCode);
        this.put(Form.PASSWORD, pin);
    }
}
