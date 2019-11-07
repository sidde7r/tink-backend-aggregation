package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.ContextEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.pair.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class NovoBancoTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final NovoBancoApiClient apiClient;

    public NovoBancoTransactionalAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = requireNonNull(apiClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> accounts = new ArrayList<>();
        GetAccountsResponse response = apiClient.getAccounts();

        Optional.of(response).map(GetAccountsResponse::getHeader).map(HeaderEntity::getContext).
                map(ContextEntity::getAccounts).map(AccountsEntity::getList)
                .map(Collection::stream).orElseGet(Stream::empty)
                .forEach(listEntity -> {
                    String internalAccountId = listEntity.getId();
                    GetAccountsResponse accountResponse = apiClient.getAccount(internalAccountId);
                    String iban = listEntity.getIban();
                    String desc = listEntity.getDesc();
                    double balance = accountResponse.getBody().getBalance().getAccounting();
                    String currency = accountResponse.getBody().getBalance().getCurrency();

                    mapToTinkAccount(internalAccountId, iban, desc, balance, currency).ifPresent(accounts::add);
                });
        return accounts;
    }

    private Optional<TransactionalAccount> mapToTinkAccount(String internalAccountId,
                                                            String iban, String name,
                                                            double balance, String currency) {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(ExactCurrencyAmount.of(balance, currency))
                        .build();

        Pair<String, AccountIdentifier.Type> accountId = getAccountIdentifier(iban, internalAccountId);

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(accountId.first)
                        .withAccountNumber(internalAccountId)
                        .withAccountName(name)
                        .addIdentifier(AccountIdentifier.create(accountId.second, accountId.first))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(getAccountType(iban))
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .build();
    }

    private Pair<String, AccountIdentifier.Type> getAccountIdentifier(String iban, String internalAccountId) {
        return Optional.ofNullable(iban)
                .map(s -> Pair.of(iban, AccountIdentifier.Type.IBAN))
                .orElse(Pair.of(internalAccountId, AccountIdentifier.Type.COUNTRY_SPECIFIC));
    }

    private TransactionalAccountType getAccountType(String iban) {
        return Optional.ofNullable(iban)
                .map(s -> TransactionalAccountType.CHECKING)
                .orElse(TransactionalAccountType.SAVINGS);
    }
}
