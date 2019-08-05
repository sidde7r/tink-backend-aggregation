package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

public interface UkOpenBankingPis {
    // Some banks do not allow us to specify a source account.
    // I.e. the user MUST pick the source account him/her self in the bank's consent flow.
    boolean mustNotHaveSourceAccountSpecified();

    PaymentResponse getBankTransferIntentId(
            UkOpenBankingApiClient apiClient, PaymentRequest paymentRequest)
            throws TransferExecutionException;

    // TODO: Remove functionality if not being used.
    void executeBankTransfer(
            UkOpenBankingApiClient apiClient,
            String intentId,
            @Nullable AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
            String referenceText)
            throws TransferExecutionException;
}
