package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEConstants.AccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
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
                .withId(getId(account))
                .setApiIdentifier(account.getProductId().getId())
                .setBankIdentifier(getIbanFormatted())
                .addHolderName(accountHolderName)
                .build();
    }

    private String getIbanFormatted() {
        return ibanNo.replaceAll(" ", "");
    }

    private PlusGiroIdentifier getPlusGiroIdentifier(AccountEntity account) {
        return new PlusGiroIdentifier(account.getProductNumber());
    }

    private ExactCurrencyAmount getBalance(AccountEntity account) {
        return ExactCurrencyAmount.of(account.getBalance(), account.getCurrency());
    }

    private IdModule getId(AccountEntity account) {
        final IdBuildStep idModule =
                IdModule.builder()
                        .withUniqueIdentifier(getIbanFormatted())
                        .withAccountNumber(account.getProductNumber())
                        .withAccountName(getAccountName(account))
                        .addIdentifier(new IbanIdentifier(getIbanFormatted()))
                        .addIdentifier(new SwedishIdentifier(longAccountNumber))
                        .setProductName(
                                AccountType.getAccountNameForCode(
                                        account.getProductTypeExtension()));
        if ("PG".equalsIgnoreCase(account.getAccountType())) {
            PlusGiroIdentifier plusGiroIdentifier = getPlusGiroIdentifier(account);
            if (plusGiroIdentifier.getGiroNumber() != null) {
                idModule.addIdentifier(plusGiroIdentifier);
            }
        }
        return idModule.build();
    }

    private String getAccountName(AccountEntity account) {
        if (!Strings.isNullOrEmpty(account.getNickName())) {
            return account.getNickName();
        }
        return accountHolderName;
    }
}
