package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BnpParibasTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BnpParibasApiBaseClient apiClient;
    private final SessionStorage sessionStorage;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;

    public BnpParibasTransactionalAccountFetcher(
            BnpParibasApiBaseClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public void setEidasProxyConfiguration(
            EidasProxyConfiguration eidasProxyConfiguration, EidasIdentity eidasIdentity) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String reqId = UUID.randomUUID().toString();
        String signature =
                BnpParibasUtils.buildSignatureHeader(
                        eidasProxyConfiguration,
                        eidasIdentity,
                        sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN),
                        reqId,
                        apiClient.getBnpParibasConfiguration());

        return Optional.ofNullable(apiClient.fetchAccounts(signature, reqId))
                .map(AccountsResponse::getAccounts).orElseGet(Collections::emptyList).stream()
                .map(
                        acc ->
                                acc.toTinkAccount(
                                        apiClient.getBalance(
                                                acc.getResourceId(), signature, reqId)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
