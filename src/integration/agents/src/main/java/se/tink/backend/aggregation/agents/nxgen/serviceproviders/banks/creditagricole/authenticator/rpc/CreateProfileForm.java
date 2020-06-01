package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Form;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class CreateProfileForm extends AbstractForm {

    public CreateProfileForm(String accountNumber, String email, String pin) {
        this.put(Form.ACCOUNT_NUMBER, accountNumber);
        this.put(Form.EMAIL, email);
        this.put(Form.PASSWORD, pin);
    }
}
