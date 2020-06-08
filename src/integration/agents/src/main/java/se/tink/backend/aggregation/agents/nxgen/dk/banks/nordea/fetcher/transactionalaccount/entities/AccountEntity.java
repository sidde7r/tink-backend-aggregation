package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AccountEntity {

    private String accountId;
    private String iban;
    private String nickname;
    private String displayAccountNumber;
    private String productCode;
    private String productName;
    private Double bookedBalance;
    private Double availableBalance;
    private Double creditLimit;

    public Optional<TransactionalAccount> toTinkAccount() {
        BalanceModule balanceModule =
                BalanceModule.builder()
                        .withBalance(getBalance())
                        .setCreditLimit(ExactCurrencyAmount.of(creditLimit, "DKK"))
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(displayAccountNumber)
                        .withAccountNumber(iban)
                        .withAccountName(nickname)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                        .build();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .setApiIdentifier(accountId)
                .putInTemporaryStorage(NordeaDkConstants.StorageKeys.PRODUCT_CODE, productCode)
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        if (availableBalance != null) {
            return ExactCurrencyAmount.of(availableBalance, "DKK");
        }
        return ExactCurrencyAmount.of(bookedBalance, "DKK");
    }

    public String getAccountId() {
        return accountId;
    }

    public String getIban() {
        return iban;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDisplayAccountNumber() {
        return displayAccountNumber;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Double getBookedBalance() {
        return bookedBalance;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }
}
