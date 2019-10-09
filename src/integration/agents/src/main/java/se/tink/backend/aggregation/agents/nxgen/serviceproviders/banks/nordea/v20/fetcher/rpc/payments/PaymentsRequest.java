package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Payment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Url;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public abstract class PaymentsRequest extends HttpRequestImpl {
    public PaymentsRequest(String accountId, Payment.StatusCode status) {
        super(
                HttpMethod.GET,
                Url.PAYMENTS
                        .queryParam("type", getTypeFrom(status))
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
