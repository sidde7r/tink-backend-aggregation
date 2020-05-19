package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SdcNoAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SdcNoApiClient bankClient;
    private final AccountNumberToIbanConverter converter;

    public SdcNoAccountFetcher(SdcNoApiClient bankClient, AccountNumberToIbanConverter converter) {
        this.bankClient = bankClient;
        this.converter = converter;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
                        .setIncludeCreditAccounts(true)
                        .setIncludeDebitAccounts(true)
                        .setOnlyFavorites(false)
                        .setOnlyQueryable(true);

        return bankClient.filterAccounts(request).getTinkAccounts(converter);
    }
}
