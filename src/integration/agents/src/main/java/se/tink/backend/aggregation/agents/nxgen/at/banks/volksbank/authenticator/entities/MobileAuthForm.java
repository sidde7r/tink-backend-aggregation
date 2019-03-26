package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class MobileAuthForm extends AbstractForm {

    public MobileAuthForm(String viewState) {
        this.put(VolksbankConstants.Form.LOGINFORM + ":shortpin", "0000");
        this.put("loginform_SUBMIT", VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_CLICK);
        this.put(
                VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.LOGINFORM + ":loginButton");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, VolksbankConstants.Form.LOGINFORM);
        this.put(VolksbankConstants.Form.LOGINFORM, VolksbankConstants.Form.LOGINFORM);
    }
}
