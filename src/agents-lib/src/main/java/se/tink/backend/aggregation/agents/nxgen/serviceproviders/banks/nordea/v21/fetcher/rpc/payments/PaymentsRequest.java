package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.rpc.payments;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Url;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public abstract class PaymentsRequest extends HttpRequestImpl {
    public PaymentsRequest(String accountId, Payment.StatusCode status) {
        super(HttpMethod.GET, Url.PAYMENTS.queryParam("type", getTypeFrom(status))
                .queryParam("accountIds", accountId));
    }

    private static String getTypeFrom(Payment.StatusCode status) {
        switch (status) {
        case CONFIRMED:
            return "list";
        case UNCONFIRMED:
            return "unconfirmed";
        default:
            throw new IllegalStateException("Unexpected status");
        }
    }
}
