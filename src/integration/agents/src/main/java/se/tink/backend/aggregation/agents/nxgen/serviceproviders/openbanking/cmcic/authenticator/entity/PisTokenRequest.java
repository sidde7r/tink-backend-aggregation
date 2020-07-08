package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PisTokenRequest extends AbstractForm {
    public PisTokenRequest(String clientId) {
        put(CmcicConstants.FormKeys.CLIENT_ID, clientId);
        put(CmcicConstants.FormKeys.GRANT_TYPE, CmcicConstants.FormValues.CLIENT_CREDENTIALS);
        put(CmcicConstants.FormKeys.SCOPE, CmcicConstants.FormValues.PIS_SCOPE);
    }
}
