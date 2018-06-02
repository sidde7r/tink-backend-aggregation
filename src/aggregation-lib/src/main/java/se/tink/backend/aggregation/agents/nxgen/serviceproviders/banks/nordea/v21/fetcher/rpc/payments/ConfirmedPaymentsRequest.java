package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.rpc.payments;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Payment;

public class ConfirmedPaymentsRequest extends PaymentsRequest {
    public ConfirmedPaymentsRequest(String accountId) {
        super(accountId, Payment.StatusCode.CONFIRMED);
    }
}
