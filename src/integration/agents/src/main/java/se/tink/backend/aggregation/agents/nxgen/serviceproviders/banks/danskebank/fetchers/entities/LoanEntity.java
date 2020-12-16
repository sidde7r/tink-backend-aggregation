package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Setter
public class LoanEntity {
    private String realEstateNumber;
    private String loanNumber;
    private int newAgreementPaymentAmount;
    private int newAgreementOutstandingDebt;
    private String agreementMark;
    private String interestMark;
    private String refinanceDate;
    private String loanExpirationDate;
    private String loanStatus;
    private String newAgreementTypeName;
    private String loanType;
    private String detailMark;
    private String currencyCode;
    private int outstandingDebt;
    private double paymentAmount;
    private String paymentFrequency;
    private String loanTypeName;
    private String propertyAddress;

    public String getRealEstateNumber() {
        return realEstateNumber;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    @JsonIgnore
    public LoanAccount toTinkLoan(LoanDetailsResponse loanDetailsResponse, String marketCode) {

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.MORTGAGE)
                                .withBalance(getBalance())
                                .withInterestRate(parseInterestRate(loanDetailsResponse))
                                .setNextDayOfTermsChange(
                                        loanDetailsResponse.getNextInterestAdjustmentDate())
                                .setNumMonthsBound(
                                        loanDetailsResponse.calculateNumberOfMonthsBound())
                                .setLoanNumber(loanNumber)
                                .setInitialBalance(loanDetailsResponse.getPrincipal(currencyCode))
                                .setSecurity(realEstateNumber)
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(loanTypeName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                getAccountIdentifierType(marketCode),
                                                getAccountNumber()))
                                .setProductName(loanTypeName)
                                .build())
                .build();
    }

    @JsonIgnore
    private AccountIdentifier.Type getAccountIdentifierType(String marketCode) {
        return Optional.ofNullable(AccountIdentifier.Type.fromScheme(marketCode.toLowerCase()))
                .orElse(AccountIdentifier.Type.COUNTRY_SPECIFIC);
    }

    /**
     * From Investopedia: The interest rate is the amount a lender charges for the use of assets
     * expressed as a percentage of the principal.
     *
     * <p>Interest rate = interest * payment frequency / cash debt (this calculation was provided by
     * mortgage expert)
     *
     * <p>Scale is rounded up to 6 decimal places, because that bank shows in the app percentage
     * interest rate with 4 decimal places - at least this is what we can assume now. Interest rate
     * returned in our data model is a number so it should be multiplied by 100 to get percentage
     * value.
     */
    @JsonIgnore
    Double parseInterestRate(LoanDetailsResponse loanDetailsResponse) {
        try {
            BigDecimal interest = new BigDecimal(loanDetailsResponse.getLoanDetail().getInterest());
            BigDecimal frequency =
                    new BigDecimal(loanDetailsResponse.getLoanDetail().getPaymentFrequency());
            BigDecimal cashDebt =
                    StringUtils.isNotBlank(loanDetailsResponse.getLoanDetail().getCashDebt())
                            ? new BigDecimal(loanDetailsResponse.getLoanDetail().getCashDebt())
                            : new BigDecimal(loanDetailsResponse.getLoanDetail().getDebtAmount());
            return interest.multiply(frequency)
                    .divide(cashDebt, 6, RoundingMode.HALF_UP)
                    .doubleValue();
        } catch (NumberFormatException | NullPointerException | ArithmeticException e) {
            return null;
        }
    }

    @JsonIgnore
    ExactCurrencyAmount getBalance() {
        ExactCurrencyAmount balance =
                ExactCurrencyAmount.of(new BigDecimal(outstandingDebt), currencyCode);
        if (balance.getExactValue().compareTo(BigDecimal.ZERO) == 0) {
            return balance;
        }

        return balance.negate();
    }

    @JsonIgnore
    private String getAccountNumber() {
        return String.format("%s-%s", realEstateNumber, loanNumber);
    }
}
