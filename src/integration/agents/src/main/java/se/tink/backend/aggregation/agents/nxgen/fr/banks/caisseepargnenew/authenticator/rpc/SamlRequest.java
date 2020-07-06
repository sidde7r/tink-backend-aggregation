package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class SamlRequest extends AbstractForm {
    public SamlRequest(String samlRequest) {
        this.put(FormKeys.SAML_REQUEST, samlRequest);
    }
}
