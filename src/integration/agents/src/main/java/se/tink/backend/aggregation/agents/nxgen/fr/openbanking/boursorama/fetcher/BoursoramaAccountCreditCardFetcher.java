package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.common.types.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.identifiers.OtherIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BoursoramaAccountCreditCardFetcher
        extends BoursoramaAccountBaseFetcher<CreditCardAccount> {
    private final BoursoramaHolderNamesExtractor boursoramaHolderNamesExtractor;

    public BoursoramaAccountCreditCardFetcher(
            BoursoramaApiClient apiClient,
            LocalDateTimeSource localDateTimeSource,
            BoursoramaHolderNamesExtractor boursoramaHolderNamesExtractor) {
        super(apiClient, localDateTimeSource);
        this.boursoramaHolderNamesExtractor = boursoramaHolderNamesExtractor;
    }

    @Override
    protected boolean filterAccountType(AccountEntity accountEntity) {
        return CashAccountType.CARD.toString().equals(accountEntity.getCashAccountType());
    }

    @Override
    protected Optional<CreditCardAccount> mapToAccount(AccountEntity accountEntity) {
        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(getCreditCardModule(accountEntity))
                        .withInferredAccountFlags()
                        .withId(getIdModule(accountEntity))
                        .setApiIdentifier(accountEntity.getResourceId())
                        .addParties(boursoramaHolderNamesExtractor.extract(accountEntity.getName()))
                        .build());
    }

    private CreditCardModule getCreditCardModule(AccountEntity accountEntity) {
        BalanceAmountEntity balance = fetchBalanceEntity(accountEntity);

        return CreditCardModule.builder()
                .withCardNumber(accountEntity.getAccountId().getOther().getIdentification())
                .withBalance(ExactCurrencyAmount.of(balance.getAmount(), balance.getCurrency()))
                .withAvailableCredit(ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR"))
                .withCardAlias(accountEntity.getProduct())
                .build();
    }

    private BalanceAmountEntity fetchBalanceEntity(AccountEntity accountEntity) {
        return apiClient.fetchBalances(accountEntity.getResourceId()).getBalances().stream()
                .findAny()
                .map(BalanceEntity::getBalanceAmount)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Could not find right type balance for account with id: "
                                                + accountEntity.getResourceId()));
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getLinkedAccount())
                .withAccountName(accountEntity.getName())
                .addIdentifier(
                        new OtherIdentifier(
                                accountEntity.getAccountId().getOther().getIdentification()))
                .setProductName(accountEntity.getProduct())
                .build();
    }
}
