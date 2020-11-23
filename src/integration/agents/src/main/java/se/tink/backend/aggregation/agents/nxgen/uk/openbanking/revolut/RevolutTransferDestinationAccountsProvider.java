package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.revolut;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransferDestinationAccountsProvider;

@RequiredArgsConstructor
public class RevolutTransferDestinationAccountsProvider
        implements TransferDestinationAccountsProvider {

    private final UkOpenBankingApiClient apiClient;

    @Override
    public List<? extends GeneralAccountEntity> getTrustedBeneficiariesAccounts(Account account) {
        return apiClient.fetchV31AccountBeneficiaries(toApiIdentifierFormat(account.getBankId()));
    }

    /**
     * this is a bit ugly solution, made necessary by the fact, that our framework removes all
     * non-alphanumeric characters from id.
     */
    private String toApiIdentifierFormat(String bankId) {
        long first = Long.parseUnsignedLong(bankId.substring(0, bankId.length() / 2), 16);
        long second = Long.parseUnsignedLong(bankId.substring(bankId.length() / 2), 16);
        return new UUID(first, second).toString();
    }
}
