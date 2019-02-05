package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GenerateBindingFinalActionForm extends AbstractForm {
    public GenerateBindingFinalActionForm(String viewState, String deviceId, String secret, String pushToken) {
        this.put("fingerprint-switch", VolksbankConstants.FALSE);
        this.put(VolksbankConstants.Form.GBFORM_SUBMIT_KEY, VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put("deviceId", deviceId);
        this.put("secret", secret);
        this.put("platform", "iOS");
        this.put("pushToken", pushToken);
        this.put(VolksbankConstants.Form.SECRET_NAME_KEY, VolksbankConstants.Form.SECRET_NAME_VALUE);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_ACTION);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY, VolksbankConstants.Form.GBFORM + ":j_id_6q");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.GBFORM);
        this.put(VolksbankConstants.Form.GBFORM, VolksbankConstants.Form.GBFORM);
    }
}
