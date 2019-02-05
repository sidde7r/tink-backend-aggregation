package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.payments;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Payment;

public class UnconfirmedPaymentsRequest extends PaymentsRequest {
    public UnconfirmedPaymentsRequest(String accountId) {
        super(accountId, Payment.StatusCode.UNCONFIRMED);
    }
}
