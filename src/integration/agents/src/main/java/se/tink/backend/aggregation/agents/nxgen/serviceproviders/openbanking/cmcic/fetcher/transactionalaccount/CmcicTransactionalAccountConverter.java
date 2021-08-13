package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountBaseConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountNameAndHolderName;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BalanceStatusEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
@Slf4j
public class CmcicTransactionalAccountConverter
        extends CmcicAccountBaseConverter<TransactionalAccount> {

    @Override
    public Optional<TransactionalAccount> convertToAccount(AccountResourceDto accountResourceDto) {
        final String iban = accountResourceDto.getAccountId().getIban();
        CmcicAccountNameAndHolderName accountNameAndHolderName =
                getAccountNameAndHolderName(accountResourceDto);
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(getBalanceModule(accountResourceDto.getBalances()))
                .withId(getIdModule(accountResourceDto, accountNameAndHolderName.getAccountName()))
                .setApiIdentifier(accountResourceDto.getResourceId())
                .setBankIdentifier(iban)
                .addHolderName(accountNameAndHolderName.getHolderName())
                .build();
    }

    private BalanceModule getBalanceModule(List<BalanceResourceDto> balances) {
        BalanceBuilderStep balanceBuilderStep =
                BalanceModule.builder().withBalance(getBookedBalance(balances));
        getAvailableBalance(balances).ifPresent(balanceBuilderStep::setAvailableBalance);
        return balanceBuilderStep.build();
    }

    private ExactCurrencyAmount getBookedBalance(List<BalanceResourceDto> balances) {
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        Optional<BalanceResourceDto> balanceEntity =
                balances.stream()
                        .filter(b -> BalanceStatusEntity.CLBD == b.getBalanceType())
                        .findAny();

        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity
                .map(Optional::of)
                .orElseGet(() -> balances.stream().findFirst())
                .map(this::getExactCurrencyAmount)
                .get();
    }

    private Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceResourceDto> balances) {
        return balances.stream()
                .filter(b -> BalanceStatusEntity.XPCD == b.getBalanceType())
                .findAny()
                .map(this::getExactCurrencyAmount);
    }

    private ExactCurrencyAmount getExactCurrencyAmount(BalanceResourceDto balanceResource) {
        return ExactCurrencyAmount.of(
                balanceResource.getBalanceAmount().getAmount(),
                balanceResource.getBalanceAmount().getCurrency());
    }
}
