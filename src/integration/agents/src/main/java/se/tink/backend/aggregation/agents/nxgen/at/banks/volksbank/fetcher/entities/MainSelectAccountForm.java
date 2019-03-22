package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class MainSelectAccountForm extends AbstractForm {

    public MainSelectAccountForm(String viewState, String productId) {
        this.put(VolksbankConstants.Form.START_PAGE_2680441_TABLE_SELECTION);
        this.put(VolksbankConstants.Form.START_PAGE_2680441_TABLE_SUBSELECTION);
        this.put(
                VolksbankConstants.Form.START_PAGE_2680441_ROW_EXPANSION,
                VolksbankConstants.BRACKET);
        this.put(VolksbankConstants.Form.START_PAGE_2680441_CLICKED_ELEMENTID, productId);
        this.put(VolksbankConstants.Form.START_PAGE_2680441_CLICKED_SUBELEMENTID);
        this.put(VolksbankConstants.Form.START_PAGE_2680441_SUBMIT, VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(
                VolksbankConstants.Form.JSF_EVENT_KEY,
                VolksbankConstants.Form.JSF_EVENT_VALUE_CHANGE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EVENT_KEY,
                VolksbankConstants.Form.JSF_EVENT_CHANGE);
        this.put(
                VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.START_PAGE_2680441_CLICKED_ELEMENTID);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.START_PAGE_2680441_CLICKED_ELEMENTID);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY,
                VolksbankConstants.Form.START_PAGE_2680441_CLICKED_ELEMENT);
        this.put(
                VolksbankConstants.Form.START_PAGE_2680441,
                VolksbankConstants.Form.START_PAGE_2680441);
    }
}
