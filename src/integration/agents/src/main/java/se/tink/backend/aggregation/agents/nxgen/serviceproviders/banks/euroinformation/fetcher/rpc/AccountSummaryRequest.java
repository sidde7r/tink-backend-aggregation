package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AccountSummaryRequest extends AbstractForm {
    public AccountSummaryRequest() {
        this.put(RequestBodyValues.WS_VERSION, RequestBodyValues.WS_VERSION_VALUE_2);
        this.put(RequestBodyValues.CATEGORIZE, RequestBodyValues.CATEGORIZE_VALUE);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
    }
}
