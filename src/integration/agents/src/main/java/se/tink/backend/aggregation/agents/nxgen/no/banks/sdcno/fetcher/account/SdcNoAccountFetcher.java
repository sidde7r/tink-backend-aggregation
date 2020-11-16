package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class SdcNoAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SdcNoApiClient bankClient;
    private final AccountNumberToIbanConverter converter;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        try {
            FilterAccountsRequest request = prepareFetchAccountRequest();
            return bankClient.filterAccounts(request).getTinkAccounts(converter);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 403) {
                log.warn("Customer does not have an access to checking / savings accounts.", e);
                return Collections.emptyList();
            } else {
                throw e;
            }
        }
    }

    private FilterAccountsRequest prepareFetchAccountRequest() {
        return new FilterAccountsRequest()
                .setIncludeCreditAccounts(true)
                .setIncludeDebitAccounts(true)
                .setOnlyFavorites(false)
                .setOnlyQueryable(true);
    }
}
