package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountDetailsEntity {
    @JsonProperty("BIC")
    private String bIC;

    private String ibanNo;
    private String accountHolderName;
    private String longAccountNumber;
    private BigDecimal creditInterestRate;
    private BigDecimal creditLimit;
    private String accruedInterest;
    private String interestPeriod;
    private String interestPaidDuringPreviousYear;
    private List<InterestRatesEntity> interestRates;

    public Optional<TransactionalAccount> toTinkAccount(AccountEntity account) {
        return TransactionalAccount.nxBuilder()
                .withType(AccountType.getAccountTypeForCode(account.getProductTypeExtension()))
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getBalance(account)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getIbanFormatted())
                                .withAccountNumber(account.getProductNumber())
                                .withAccountName(account.getNickName())
                                .addIdentifier(new IbanIdentifier(getIbanFormatted()))
                                .addIdentifier(new SwedishIdentifier(longAccountNumber))
                                .addIdentifier(getPlusGiroIdentifier(account))
                                .setProductName(
                                        AccountType.getAccountNameForCode(
                                                account.getProductTypeExtension()))
                                .build())
                .setApiIdentifier(account.getProductId().getId())
                .setBankIdentifier(getIbanFormatted())
                .addHolderName(accountHolderName)
                .build();
    }

    private String getIbanFormatted() {
        return ibanNo.replaceAll(" ", "");
    }

    private PlusGiroIdentifier getPlusGiroIdentifier(AccountEntity account) {
        if ("PG".equalsIgnoreCase(account.getAccountType())) {
            return new PlusGiroIdentifier(account.getProductNumber());
        }
        return null;
    }

    private ExactCurrencyAmount getBalance(AccountEntity account) {
        return ExactCurrencyAmount.of(account.getBalance(), account.getCurrency());
    }
}
