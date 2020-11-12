package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.IdentificationType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
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
                .collect(Collectors.toList());
    }

    private CreditCardAccount toCreditCardAccount(AccountEntity accountEntity) {
        final CrosskeyAccountBalancesResponse crosskeyAccountBalancesResponse =
                apiClient.fetchAccountBalances(accountEntity.getAccountId());

        final Optional<AccountDetailsEntity> accountDetails =
                accountEntity.getAccountDetails(IdentificationType.CREDIT_CARD);
        final AmountEntity amount =
                crosskeyAccountBalancesResponse.getData().getInterimAvailableBalance().getAmount();

        return getCreditCardAccount(accountEntity, accountDetails, amount);
    }

    private CreditCardAccount getCreditCardAccount(
            AccountEntity accountEntity,
            Optional<AccountDetailsEntity> accountDetails,
            AmountEntity amount) {

        final String maskedCardNumber =
                accountDetails
                        .map(AccountDetailsEntity::getIdentification)
                        .orElse(accountEntity.getAccountId());

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(maskedCardNumber)
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                BigDecimal.valueOf(amount.getAmount()),
                                                amount.getCurrency()))
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(
                                                BigDecimal.ZERO, amount.getCurrency()))
                                .withCardAlias(accountEntity.getDescription())
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(maskedCardNumber)
                                .withAccountNumber(maskedCardNumber)
                                .withAccountName(accountEntity.getDescription())
                                .addIdentifier(new PaymentCardNumberIdentifier(maskedCardNumber))
                                .setProductName(accountEntity.getDescription())
                                .build())
                .setApiIdentifier(accountEntity.getAccountId())
                .build();
    }
}
