package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPis;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

public class UkOpenBankingV31Pis implements UkOpenBankingPis {
    @Override
    public boolean mustNotHaveSourceAccountSpecified() {
        return false;
    }

    @Override
    public String getBankTransferIntentId(UkOpenBankingApiClient apiClient,
            @Nullable AccountIdentifier sourceIdentifier, AccountIdentifier destinationIdentifier, Amount amount,
            String referenceText) throws TransferExecutionException {
        return null;
    }

    @Override
    public void executeBankTransfer(UkOpenBankingApiClient apiClient, String intentId,
            @Nullable AccountIdentifier sourceIdentifier, AccountIdentifier destinationIdentifier, Amount amount,
            String referenceText) throws TransferExecutionException {

    }
}
