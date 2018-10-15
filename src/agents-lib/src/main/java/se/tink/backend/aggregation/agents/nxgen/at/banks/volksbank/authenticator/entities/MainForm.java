package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class MainForm extends AbstractForm {
    public MainForm(String viewState) {
        this.put(VolksbankConstants.Form.START_SITE + ":from-position");
        this.put(VolksbankConstants.Form.START_SITE + ":to-position");
        this.put("startseite_SUBMIT", "1");
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_ACTION);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY, VolksbankConstants.Form.START_SITE + ":refresh");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.START_SITE+ ":refresh");
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, "startseite-repeated-item-2680441:actual-widget startseite-repeated-item-2680442:actual-widget startseite-repeated-item-2680443:actual-widget");
        this.put(VolksbankConstants.Form.START_SITE, VolksbankConstants.Form.START_SITE);
    }
}
