package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbAccountMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class DnbAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final DnbStorage storage;
    private final DnbApiClient apiClient;
    private final DnbAccountMapper accountMapper;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String consentId = storage.getConsentId();
        return apiClient.fetchAccounts(consentId).getAccounts().stream()
                .map(
                        acc ->
                                accountMapper.toTinkAccount(
                                        acc, apiClient.fetchBalances(consentId, acc.getBban())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
