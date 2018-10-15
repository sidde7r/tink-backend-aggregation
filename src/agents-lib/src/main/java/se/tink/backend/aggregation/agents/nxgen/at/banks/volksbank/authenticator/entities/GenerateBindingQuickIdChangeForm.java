package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GenerateBindingQuickIdChangeForm extends AbstractForm {
    public GenerateBindingQuickIdChangeForm(String viewState) {
        this.put(VolksbankConstants.Form.SHORTPIN_SWITCH_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.GBFORM_SUBMIT_KEY, "1");
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_CHANGE);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.GBFORM + ":geraetebindung-herstellen-einstellungen:shortpinswitch:switch");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.GBFORM + ":geraetebindung-herstellen-einstellungen:shortpinswitch:switch");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, VolksbankConstants.Form.GBFORM);
        this.put(VolksbankConstants.Form.GBFORM, VolksbankConstants.Form.GBFORM);
    }
}
