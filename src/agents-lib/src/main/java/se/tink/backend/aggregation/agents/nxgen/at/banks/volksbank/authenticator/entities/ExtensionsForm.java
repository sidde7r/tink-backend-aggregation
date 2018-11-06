package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ExtensionsForm extends AbstractForm {
    public ExtensionsForm(String viewState) {
        this.put(VolksbankConstants.Form.MBEFORM_SUBMIT_KEY, VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_ACTION);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.MBEFORM + VolksbankConstants.COLUMN + VolksbankConstants.Form.ENTRIES_MBE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.MBEFORM + VolksbankConstants.COLUMN + VolksbankConstants.Form.ENTRIES_MBE);
        this.put(VolksbankConstants.Form.MBEFORM, VolksbankConstants.Form.MBEFORM);
    }
}
