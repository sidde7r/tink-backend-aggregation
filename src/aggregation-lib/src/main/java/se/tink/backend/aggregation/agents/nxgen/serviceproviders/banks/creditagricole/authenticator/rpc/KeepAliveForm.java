package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.Form;

public class KeepAliveForm extends AbstractForm {
    public KeepAliveForm SetLlToken(String llToken) {
        this.put(Form.LL_TOKEN, llToken);
        return this;
    }
}
