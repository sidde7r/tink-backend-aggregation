package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.MortgageDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanAccountEntity {

    private static final int VALUE_DECIMAL_PLACES = 2;
    private static final int RATE_DECIMAL_PLACES = 5;

    private String accountHolders;
    private String accountImageUrl;
    private String accountNumber;
    private String accountType;
    private String alias;
    private String currency;
    private String description;
    private String fullAccountKey;
    private String fullDepositAccountKey;
    private String iban;
    private String productCode;

    public LoanAccount mortgageToTinkAccount(MortgageDetailsResponse details) {
        BigDecimal interestRate = details.getAnualRate().movePointLeft(RATE_DECIMAL_PLACES);
        BigDecimal balance = details.getAmountOverdue().movePointLeft(VALUE_DECIMAL_PLACES);
        BigDecimal amortizedAmount = calculateAmountAmortized(details);
        BigDecimal initialBalance =
                details.getAmountContracted().movePointLeft(VALUE_DECIMAL_PLACES);
        BigDecimal monthlyAmortization =
                details.getAmountNextInstallment().movePointLeft(VALUE_DECIMAL_PLACES);

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(Type.MORTGAGE)
                                .withBalance(ExactCurrencyAmount.of(balance, currency))
                                .withInterestRate(interestRate.doubleValue())
                                .setNumMonthsBound(calculateNumMonthsBound(details))
                                .setInitialDate(convert(details.getDateCreated()))
                                .setInitialBalance(ExactCurrencyAmount.of(initialBalance, currency))
                                .setAmortized(ExactCurrencyAmount.of(amortizedAmount, currency))
                                .setMonthlyAmortization(
                                        ExactCurrencyAmount.of(monthlyAmortization, currency))
                                .build())
                .withId(buildId(details))
                .setApiIdentifier(fullAccountKey)
                .putInTemporaryStorage(STORAGE.ACCOUNT_CURRENCY, currency)
                .build();
    }

    private BigDecimal calculateAmountAmortized(MortgageDetailsResponse details) {
        return details.getAmountContracted()
                .subtract(details.getAmountOverdue())
                .movePointLeft(VALUE_DECIMAL_PLACES);
    }

    private IdModule buildId(MortgageDetailsResponse details) {
        return IdModule.builder()
                .withUniqueIdentifier(fullAccountKey)
                .withAccountNumber(accountNumber)
                .withAccountName(description)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.COUNTRY_SPECIFIC, accountNumber))
                .setProductName(details.getLoanType())
                .build();
    }

    private int calculateNumMonthsBound(MortgageDetailsResponse details) {
        return (int)
                ChronoUnit.MONTHS.between(
                        convert(details.getDateCreated()), convert(details.getLoanEndDate()));
    }

    public String getFullAccountKey() {
        return fullAccountKey;
    }

    private LocalDate convert(Date date) {
        return date.toInstant().atZone(ZoneId.of(CaixaConstants.TIMEZONE_ID)).toLocalDate();
    }
}
