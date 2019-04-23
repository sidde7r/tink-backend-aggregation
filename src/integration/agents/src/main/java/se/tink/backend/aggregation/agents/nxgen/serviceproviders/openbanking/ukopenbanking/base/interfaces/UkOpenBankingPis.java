package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

public interface UkOpenBankingPis {
    // Some banks do not allow us to specify a source account.
    // I.e. the user MUST pick the source account him/her self in the bank's consent flow.
    boolean mustNotHaveSourceAccountSpecified();

    String getBankTransferIntentId(
            UkOpenBankingApiClient apiClient,
            @Nullable AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
            String referenceText)
            throws TransferExecutionException;

    void executeBankTransfer(
            UkOpenBankingApiClient apiClient,
            String intentId,
            @Nullable AccountIdentifier sourceIdentifier,
            AccountIdentifier destinationIdentifier,
            Amount amount,
            String referenceText)
            throws TransferExecutionException;
}
