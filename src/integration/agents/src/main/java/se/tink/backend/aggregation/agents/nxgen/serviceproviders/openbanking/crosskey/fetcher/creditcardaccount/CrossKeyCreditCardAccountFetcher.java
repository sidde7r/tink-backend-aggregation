package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

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
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.creditcard.CreditCardBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;

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
            AccountEntity accountEntity, AccountBalancesDataEntity accountBalancesDataEntity) {

        Optional<AccountDetailsEntity> accountDetails =
                accountEntity.getAccountDetails(IdentificationType.CREDIT_CARD);
        final String maskedCardNumber =
                accountDetails
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        Optional<AccountBalanceEntity> maybeBalanceEntity =
                accountBalancesDataEntity.getInterimAvailableBalance();
        if (!maybeBalanceEntity.isPresent()) {
            return Optional.empty();
        }
        AccountBalanceEntity balanceEntity = maybeBalanceEntity.get();
        CreditCardBuildStep creditCardAccount =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(maskedCardNumber)
                                        .withBalance(balanceEntity.getExactAmount())
                                        .withAvailableCredit(balanceEntity.getExactAmount())
                                        .withCardAlias(accountEntity.getDescription())
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(maskedCardNumber)
                                        .withAccountNumber(maskedCardNumber)
                                        .withAccountName(accountEntity.getDescription())
                                        .addIdentifier(
                                                new PaymentCardNumberIdentifier(maskedCardNumber))
                                        .setProductName(accountEntity.getDescription())
                                        .build())
                        .setApiIdentifier(accountEntity.getAccountId());

        accountDetails
                .map(AccountDetailsEntity::getName)
                .ifPresent(creditCardAccount::addHolderName);

        return Optional.of(creditCardAccount.build());
    }
}
