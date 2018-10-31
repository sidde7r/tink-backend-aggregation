package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.entities;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class MainFetchTransactionsForDatesChangeForm extends AbstractForm {
    public MainFetchTransactionsForDatesChangeForm(String viewState, Date startDate, Date endDate) {
        this.put(VolksbankConstants.Form.OVERLAY_DATE_FROM,
                ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.format(startDate));
        this.put(VolksbankConstants.Form.OVERLAY_DATE_TO, ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.format(endDate));
        this.put(VolksbankConstants.Form.OVERLAY_DATE_IS_RANGE, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.OVERLAY_SUBMIT, VolksbankConstants.ONE);
        this.put(VolksbankConstants.Form.JSF_VIEWSTATE_KEY, viewState);
        this.put(VolksbankConstants.Form.JSF_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_VALUE_CHANGE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EVENT_KEY, VolksbankConstants.Form.JSF_EVENT_CHANGE);
        this.put(VolksbankConstants.Form.JSF_SOURCE_KEY, VolksbankConstants.Form.OVERLAY_DATE_FROM);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_AJAX_KEY, VolksbankConstants.TRUE);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_EXECUTE_KEY, VolksbankConstants.Form.OVERLAY_DATE_FROM);
        this.put(VolksbankConstants.Form.JSF_PARTIAL_RENDER_KEY, VolksbankConstants.Form.OVERLAY_RENDER);
        this.put(VolksbankConstants.Form.OVERLAY, VolksbankConstants.Form.OVERLAY);
    }
}
