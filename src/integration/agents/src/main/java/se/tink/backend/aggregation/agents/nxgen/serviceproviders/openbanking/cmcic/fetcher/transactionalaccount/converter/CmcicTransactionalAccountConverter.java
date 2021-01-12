package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.converter;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BalanceStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CashAccountTypeEnumEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
@Slf4j
public class CmcicTransactionalAccountConverter {

    private static final List<BalanceStatusEntity> BALANCE_PREFERRED_TYPES =
            ImmutableList.of(BalanceStatusEntity.XPCD, BalanceStatusEntity.CLBD);

    private final PrioritizedValueExtractor valueExtractor;

    public Optional<TransactionalAccount> convertAccountResourceToTinkAccount(
            AccountResourceDto accountResource) {
        if (accountResource.getCashAccountType() != CashAccountTypeEnumEntity.CACC) {
            log.info("Account type different than CACC.");
            return Optional.empty();
        }

        final String iban = accountResource.getAccountId().getIban();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance(accountResource)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(accountResource.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                                .build())
                .setApiIdentifier(accountResource.getResourceId())
                .setBankIdentifier(iban)
                .build();
    }

    private ExactCurrencyAmount getBalance(AccountResourceDto accountResource) {
        return valueExtractor
                .pickByValuePriority(
                        accountResource.getBalances(),
                        BalanceResourceDto::getBalanceType,
                        BALANCE_PREFERRED_TYPES)
                .map(CmcicTransactionalAccountConverter::getExactCurrencyAmount)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account balance. No available balance with type of: "
                                                + StringUtils.join(BALANCE_PREFERRED_TYPES, ", ")));
    }

    private static ExactCurrencyAmount getExactCurrencyAmount(BalanceResourceDto balanceResource) {
        return ExactCurrencyAmount.of(
                balanceResource.getBalanceAmount().getAmount(),
                balanceResource.getBalanceAmount().getCurrency());
    }
}
