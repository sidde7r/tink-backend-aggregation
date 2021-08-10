package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.creditcard;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountBaseConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.converter.CmcicAccountNameAndHolderName;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CmcicCreditCardConverter extends CmcicAccountBaseConverter<CreditCardAccount> {

    @Override
    public Optional<CreditCardAccount> convertToAccount(AccountResourceDto accountResourceDto) {
        CmcicAccountNameAndHolderName nameAndHolderName =
                getAccountNameAndHolderName(accountResourceDto);
        return Optional.of(
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber(accountResourceDto.getAccountId().getIban())
                                        .withBalance(getBalanceCreditCard(accountResourceDto))
                                        .withAvailableCredit(getEmptyBalance())
                                        .withCardAlias(nameAndHolderName.getAccountName())
                                        .build())
                        .withInferredAccountFlags()
                        .withId(getIdModule(accountResourceDto, nameAndHolderName.getAccountName()))
                        .setApiIdentifier(accountResourceDto.getResourceId())
                        .addHolderName(nameAndHolderName.getHolderName())
                        .build());
    }

    private ExactCurrencyAmount getBalanceCreditCard(AccountResourceDto accountResourceDto) {
        return accountResourceDto.getBalances().stream()
                .map(
                        balance ->
                                ExactCurrencyAmount.of(
                                        balance.getBalanceAmount().getAmount(),
                                        balance.getBalanceAmount().getCurrency()))
                .findFirst()
                .orElse(getEmptyBalance());
    }

    private ExactCurrencyAmount getEmptyBalance() {
        return ExactCurrencyAmount.of(0, "EUR");
    }
}
