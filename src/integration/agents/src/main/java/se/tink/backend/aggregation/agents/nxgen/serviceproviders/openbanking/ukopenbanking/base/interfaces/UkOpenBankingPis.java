package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public interface UkOpenBankingPis {
    // Some banks do not allow us to specify a source account.
    // I.e. the user MUST pick the source account him/her self in the bank's consent flow.
    boolean mustNotHaveSourceAccountSpecified();

    PaymentResponse setupPaymentOrderConsent(
            UkOpenBankingApiClient apiClient, PaymentRequest paymentRequest)
            throws TransferExecutionException;
}
