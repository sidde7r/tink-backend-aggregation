package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.converter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CashAccountTypeEnumEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CmcicCreditCardConverter {

    public CreditCardAccount convertToCreditCard(AccountResourceDto accountResourceDto) {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountResourceDto.getAccountId().getIban())
                                .withBalance(getBalanceCreditCard(accountResourceDto))
                                .withAvailableCredit(getAvailableCreditCreditCard())
                                .withCardAlias(accountResourceDto.getName())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountResourceDto.getResourceId())
                                .withAccountNumber(accountResourceDto.getAccountId().getIban())
                                .withAccountName(accountResourceDto.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                Type.PAYMENT_CARD_NUMBER,
                                                accountResourceDto.getAccountId().getIban()))
                                .setProductName(accountResourceDto.getName())
                                .build())
                .setApiIdentifier(accountResourceDto.getResourceId())
                .build();
    }

    public boolean isCreditCard(AccountResourceDto accountResourceDto) {
        return CashAccountTypeEnumEntity.CARD == accountResourceDto.getCashAccountType();
    }

    private ExactCurrencyAmount getBalanceCreditCard(AccountResourceDto accountResourceDto) {
        return accountResourceDto.getBalances().stream()
                .map(
                        balance ->
                                ExactCurrencyAmount.of(
                                        balance.getBalanceAmount().getAmount(),
                                        balance.getBalanceAmount().getCurrency()))
                .findFirst()
                .orElse(null);
    }

    private ExactCurrencyAmount getAvailableCreditCreditCard() {
        return ExactCurrencyAmount.of(0, "EUR");
    }
}
