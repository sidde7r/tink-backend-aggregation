package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.RequestBodyValues;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TransactionSummaryRequest extends AbstractForm {

    public TransactionSummaryRequest(String webId) {
        this.put(RequestBodyValues.WEB_ID, webId);
        this.put(RequestBodyValues.WS_VERSION, RequestBodyValues.WS_VERSION_VALUE_1);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
    }

    public TransactionSummaryRequest(String webId, String recoveryKey) {
        this.put(RequestBodyValues.WEB_ID, webId);
        this.put(RequestBodyValues.WS_VERSION, RequestBodyValues.WS_VERSION_VALUE_1);
        this.put(RequestBodyValues.MEDIA, RequestBodyValues.MEDIA_VALUE);
        this.put(RequestBodyValues.MAX_ITEMS, RequestBodyValues.MAX_ITEMS_VALUE);
        this.put(RequestBodyValues.RECOVERY_KEY, recoveryKey == null ? "" : recoveryKey);
    }
}
