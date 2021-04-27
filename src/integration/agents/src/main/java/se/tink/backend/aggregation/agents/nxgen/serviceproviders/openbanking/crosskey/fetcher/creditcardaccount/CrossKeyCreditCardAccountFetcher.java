package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.IdentificationType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.accountbalances.AccountBalancesDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.MaskedPanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CrossKeyCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final CrosskeyBaseApiClient apiClient;

    public CrossKeyCreditCardAccountFetcher(CrosskeyBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getData().getAccounts().stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(this::toCreditCardAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<CreditCardAccount> toCreditCardAccount(AccountEntity accountEntity) {
        final CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse =
                apiClient.fetchAccountBalances(accountEntity.getAccountId());

        final AccountBalancesDataEntity balances = crosskeyAccountBalancesResponse.getData();
        return getCreditCardAccount(accountEntity, balances);
    }

    private Optional<CreditCardAccount> getCreditCardAccount(
            AccountEntity accountEntity, AccountBalancesDataEntity balances) {

        final String maskedCardNumber =
                accountEntity
                        .getAccountDetails(IdentificationType.CREDIT_CARD)
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        Optional<ExactCurrencyAmount> balance = getBalance(balances);

        if (!balance.isPresent()) {
            return Optional.empty();
        }
        ExactCurrencyAmount exactCurrencyAmount = balance.get();

        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(maskedCardNumber)
                                        .withBalance(exactCurrencyAmount)
                                        .withAvailableCredit(
                                                ExactCurrencyAmount.of(
                                                        BigDecimal.ZERO,
                                                        exactCurrencyAmount.getCurrencyCode()))
                                        .withCardAlias(accountEntity.getDescription())
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(maskedCardNumber)
                                        .withAccountNumber(maskedCardNumber)
                                        .withAccountName(accountEntity.getDescription())
                                        .addIdentifier(new MaskedPanIdentifier(maskedCardNumber))
                                        .setProductName(accountEntity.getDescription())
                                        .build())
                        .setApiIdentifier(accountEntity.getAccountId())
                        .build());
    }

    private Optional<ExactCurrencyAmount> getBalance(AccountBalancesDataEntity balances) {
        Optional<AccountBalanceEntity> interimBookedBalance = balances.getInterimBookedBalance();
        if (interimBookedBalance.isPresent()) {
            return interimBookedBalance.map(AccountBalanceEntity::getExactAmount);
        }
        return balances.getInterimAvailableBalance().map(AccountBalanceEntity::getExactAmount);
    }
}
