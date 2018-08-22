package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TransactionSummaryRequest extends AbstractForm {
    public TransactionSummaryRequest(String webId) {
        this.put(RequestBodyValues.WEB_ID, webId);
        this.put(RequestBodyValues.WS_VERSION, RequestBodyValues.WS_VERSION_VALUE_1);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
    }
}
