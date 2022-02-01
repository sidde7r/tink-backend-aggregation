package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.base.BredBanquePopulaireBaseAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.common.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class BredBanquePopulaireTransactionalAccountFetcher
        extends BredBanquePopulaireBaseAccountFetcher<TransactionalAccount> {

    public BredBanquePopulaireTransactionalAccountFetcher(BredBanquePopulaireApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected boolean filterAccountType(AccountEntity accountEntity) {
        return accountEntity.isTransactionalAccount();
    }

    @Override
    protected Optional<TransactionalAccount> mapToAccount(AccountEntity accountEntity) {
        List<BalanceEntity> balances =
                accountEntity.containsBalances()
                        ? accountEntity.getBalances()
                        : fetchBalances(accountEntity.getResourceId());

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(balances))
                .withId(getIdModule(accountEntity))
                .setApiIdentifier(accountEntity.getResourceId())
                .setBankIdentifier(accountEntity.getBicFi())
                .build();
    }

    private List<BalanceEntity> fetchBalances(String accountResourceId) {
        return Optional.ofNullable(apiClient.fetchBalances(accountResourceId))
                .map(BalancesResponse::getBalances)
                .orElse(Collections.emptyList());
    }

    private BalanceModule getBalanceModule(List<BalanceEntity> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getAccountId().getIban())
                .withAccountNumber(accountEntity.getAccountId().getIban())
                .withAccountName(accountEntity.getName())
                .addIdentifier(
                        new IbanIdentifier(
                                accountEntity.getBicFi(), accountEntity.getAccountId().getIban()))
                .build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalanceEntity> bookedBalanceEntity =
                balances.stream()
                        .filter(entity -> BalanceType.CLBD.equals(entity.getBalanceType()))
                        .findAny();

        if (!bookedBalanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }

        return bookedBalanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balances) {
        return balances.stream()
                .filter(balanceEntity -> BalanceType.XPCD.equals(balanceEntity.getBalanceType()))
                .findAny()
                .map(BalanceEntity::getBalanceAmount)
                .map(AmountEntity::toTinkAmount);
    }
}
