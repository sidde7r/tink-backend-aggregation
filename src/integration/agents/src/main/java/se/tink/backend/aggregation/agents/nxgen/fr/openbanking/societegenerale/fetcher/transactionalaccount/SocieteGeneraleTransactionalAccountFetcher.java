package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class SocieteGeneraleTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final SocieteGeneraleApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final SignatureHeaderProvider signatureHeaderProvider;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                signatureHeaderProvider.buildSignatureHeader(
                        sessionStorage.get(StorageKeys.TOKEN), reqId);

        EndUserIdentityResponse user = apiClient.getEndUserIdentity(signature, reqId);

        return Optional.ofNullable(apiClient.fetchAccounts(signature, reqId))
                .map(AccountsResponse::getCashAccounts).orElseGet(Collections::emptyList).stream()
                .map(accountsItem -> accountsItem.toTinkModel(user.getConnectedPsu()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
