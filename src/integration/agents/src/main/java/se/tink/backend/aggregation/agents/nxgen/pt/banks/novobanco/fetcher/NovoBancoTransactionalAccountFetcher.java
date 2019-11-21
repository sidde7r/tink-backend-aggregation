package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.ContextEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NovoBancoTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final NovoBancoApiClient apiClient;

    public NovoBancoTransactionalAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = requireNonNull(apiClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        GetAccountsResponse response = apiClient.getAccounts();

        Optional.of(response)
                .map(GetAccountsResponse::getHeader)
                .map(HeaderEntity::getContext)
                .map(ContextEntity::getAccounts)
                .map(AccountsEntity::getList)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .forEach(
                        listEntity -> {
                            String internalAccountId = listEntity.getId();
                            GetAccountsResponse accountResponse =
                                    apiClient.getAccount(internalAccountId);
                            String iban = listEntity.getIban();
                            String desc = listEntity.getDesc();
                            double balance = accountResponse.getBody().getBalance().getAccounting();
                            String currency = accountResponse.getBody().getBalance().getCurrency();

                            TransactionalAccountMapper.mapToTinkAccount(
                                            internalAccountId, iban, desc, balance, currency)
                                    .ifPresent(accounts::add);
                        });
        return accounts;
    }
}
