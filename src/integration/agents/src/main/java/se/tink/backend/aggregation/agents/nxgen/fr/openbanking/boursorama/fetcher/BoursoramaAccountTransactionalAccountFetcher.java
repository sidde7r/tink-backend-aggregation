package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.CashAccountType;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class BoursoramaAccountTransactionalAccountFetcher
        extends BoursoramaAccountBaseFetcher<TransactionalAccount> {

    private static final String INSTANT_BALANCE = "XPCD";
    private static final String ACCOUNTING_BALANCE = "CLBD";

    public BoursoramaAccountTransactionalAccountFetcher(
            BoursoramaApiClient apiClient, LocalDateTimeSource localDateTimeSource) {
        super(apiClient, localDateTimeSource);
    }

    @Override
    protected boolean filterAccountType(AccountEntity accountEntity) {
        return CashAccountType.CACC.toString().equals(accountEntity.getCashAccountType());
    }

    @Override
    protected Optional<TransactionalAccount> mapToAccount(AccountEntity accountEntity) {
        List<BalanceEntity> balances =
                apiClient.fetchBalances(accountEntity.getResourceId()).getBalances();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(balances))
                .withId(getIdModule(accountEntity))
                .setApiIdentifier(accountEntity.getResourceId())
                .addHolderName(removeCourtesyTitle(accountEntity.getName()))
                .build();
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getResourceId())
                .withAccountNumber(accountEntity.getAccountId().getIban())
                .withAccountName(accountEntity.getProduct())
                .addIdentifier(
                        new IbanIdentifier(
                                accountEntity.getBicFi(), accountEntity.getAccountId().getIban()))
                .setProductName(accountEntity.getProduct())
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }

        Optional<BalanceEntity> balanceEntity =
                balances.stream()
                        .filter(b -> ACCOUNTING_BALANCE.equals(b.getBalanceType()))
                        .findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }

        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalanceEntity::getBalanceAmount)
                .map(
                        balanceAmount ->
                                ExactCurrencyAmount.of(
                                        balanceAmount.getAmount(), balanceAmount.getCurrency()))
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(b -> INSTANT_BALANCE.equals(b.getBalanceType()))
                .findAny()
                .map(BalanceEntity::getBalanceAmount)
                .map(
                        balanceAmountEntity ->
                                ExactCurrencyAmount.of(
                                        balanceAmountEntity.getAmount(),
                                        balanceAmountEntity.getCurrency()));
    }
}
