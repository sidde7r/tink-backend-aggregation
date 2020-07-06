package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.FormValues;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class TokenRequest extends AbstractForm {
    public TokenRequest() {
        this.put(FormKeys.CLIENT_ID, FormValues.CLIENT_ID);
        this.put(FormKeys.CLIENT_SECRET, FormValues.CLIENT_SECRET);
        this.put(FormKeys.GRANT_TYPE, FormValues.GRANT_CLIENT_CREDENTIALS);
    }
}
