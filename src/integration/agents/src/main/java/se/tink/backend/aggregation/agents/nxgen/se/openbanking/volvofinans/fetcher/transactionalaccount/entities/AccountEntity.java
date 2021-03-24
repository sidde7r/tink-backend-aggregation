package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    private String resourceId;
    private String accountNumber;
    private String currency;
    private String product;
    private String ownerName;
    private List<BalanceEntity> balances;
    private String accountType;
    private String status;
    private BalanceAmountEntity creditLimit;

    public boolean isEnabled() {
        return status.equalsIgnoreCase(VolvoFinansConstants.Accounts.STATUS_ENABLED);
    }

    public CreditCardAccount toCreditCardAccount() {

        // To avoid NPE at the absence of "balances" if the account is not belonged to account
        // holder
        Double availableCredit = 0.00;

        if (Optional.ofNullable(balances).isPresent()) {
            availableCredit =
                    Double.parseDouble(creditLimit.getAmount()) + getBalance().getDoubleValue();
        }

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountNumber)
                                .withBalance(getBalance())
                                .withAvailableCredit(
                                        ExactCurrencyAmount.of(availableCredit, currency))
                                .withCardAlias(product)
                                .build())
                .withoutFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(product)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, accountNumber))
                                .setProductName(product)
                                .build())
                .putInTemporaryStorage(VolvoFinansConstants.StorageKeys.ACCOUNT_ID, resourceId)
                .addHolderName(ownerName)
                .setApiIdentifier(resourceId)
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(BalanceEntity::isExpected)
                .findFirst()
                .map(BalanceEntity::getAmount)
                .orElse(ExactCurrencyAmount.zero(currency));
    }
}
