package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.FormKeys;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class SamlRequest extends AbstractForm {
    public SamlRequest(String samlRequest) {
        this.put(FormKeys.SAML_RESPONSE, samlRequest);
    }
}
