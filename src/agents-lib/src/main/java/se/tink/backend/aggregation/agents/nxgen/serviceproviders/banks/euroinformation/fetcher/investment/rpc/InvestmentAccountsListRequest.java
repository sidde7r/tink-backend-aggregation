package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class InvestmentAccountsListRequest extends AbstractForm {
    public InvestmentAccountsListRequest(int page) {
        this.put(RequestBodyValues.WS_VERSION, RequestBodyValues.WS_VERSION_VALUE_7);
        this.put(RequestBodyValues.CATEGORIZE, RequestBodyValues.CATEGORIZE_VALUE);
        this.put(RequestBodyValues.CURRENT_PAGE, String.valueOf(page));
        this.put(RequestBodyValues.MAX_ELEMENTS, RequestBodyValues.MAX_ELEMENTS_VALUE);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
    }
}
