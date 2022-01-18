package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;

@AllArgsConstructor
@Slf4j
public class CbiAccountFetchingStep {

    private final CbiGlobeFetcherApiClient fetcherApiClient;
    private final CbiStorage storage;

    // CBI only allows fetching accounts once for recurring consent, and only in a short time after
    // consent creation.
    // We fetch those during auth to ensure we are actually able to get the data.
    public void fetchAndSaveAccounts() {
        AccountsResponse accountsResponse = fetcherApiClient.getAccounts();

        AccountsResponse accountsResponsePruned =
                new AccountsResponse(
                        removeAccountsWithIncompleteData(accountsResponse.getAccounts()));

        storage.saveAccountsResponse(accountsResponsePruned);
    }

    private List<AccountEntity> removeAccountsWithIncompleteData(
            List<AccountEntity> originalAccounts) {
        // This method tries its best to filter out the accounts that are definitely wrong, ie. do
        // not have very basic data, before we even save it to storage
        int sizeBeforePruning = originalAccounts.size();

        List<AccountEntity> prunedAccounts =
                originalAccounts.stream()
                        .filter(x -> !x.isEmptyAccountObject())
                        .collect(Collectors.toList());
        int sizeAfterPruning = prunedAccounts.size();

        // I'm not sure if this still happens, lets leave a log line to observe.
        if (sizeAfterPruning != sizeBeforePruning) {
            log.info("[CBI] Account list pruned during auth due to missing data!");
        }
        return prunedAccounts;
    }
}
