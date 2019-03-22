package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class MainFetchTransactionsGeneralCustomForm extends AbstractForm {

    public MainFetchTransactionsGeneralCustomForm(String viewState) {
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_INLINE, "GENERAL_CUSTOM");
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_FILTER, "ALLE");
        this.put(
                VolksbankConstants.Form.KONTO_UMSATZ_SORTBOX,
                VolksbankConstants.Form.KONTO_UMSATZ_DESC);
        this.put(
                VolksbankConstants.Form.KONTO_UMSATZ_SORTBOXXS,
                VolksbankConstants.Form.KONTO_UMSATZ_DESC);
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_TABLE_SELECTION, VolksbankConstants.BRACKET);
        this.put(
                VolksbankConstants.Form.KONTO_UMSATZ_TABLE_SUBSELECTION,
                VolksbankConstants.BRACKET);
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_ROW_EXPANSION, VolksbankConstants.BRACKET);
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_CLICKED_ELEMENTID);
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_CLICKED_SUBELEMENTID);
        this.put(VolksbankConstants.Form.KONTO_UMSATZ_SUBMIT, VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(
                VolksbankConstants.Form.JSF_EVENT_KEY,
                VolksbankConstants.Form.JSF_EVENT_VALUE_CHANGE);
        this.put(
                VolksbankConstants.Form.JSF_SOURCE_KEY,
                VolksbankConstants.Form.KONTO_UMSATZ_INLINE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(
                VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY,
                VolksbankConstants.Form.KONTO_UMSATZ_INLINE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, "contentarea");
        this.put(VolksbankConstants.Form.KONTO_UMSATZ, VolksbankConstants.Form.KONTO_UMSATZ);
    }
}
