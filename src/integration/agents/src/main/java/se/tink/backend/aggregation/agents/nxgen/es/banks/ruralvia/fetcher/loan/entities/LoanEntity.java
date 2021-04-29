package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils.parseAmountInEuros;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import se.tink.backend.aggregation.agents.exceptions.refresh.LoanAccountRefreshException;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Getter
@Builder(toBuilder = true)
public class LoanEntity {

    String accountNumber;
    String description;
    @Builder.Default String currency = "EUR";
    String currentBalance;
    String initialBalance;
    String amortizedAmount;
    LoanDetails.Type accountType;
    @Singular private List<String> applicants;
    String interestRate;
    String deductionDate; // the next payment date for the loan
    String startDate; // formalization date
    String endDate;
    String monthlyAmortization;

    public LoanAccount toTinkLoanAccount() {

        LocalDate initialDate = LocalDate.parse(getStartDate(), PATTERN);
        LocalDate finalDate = LocalDate.parse(getEndDate(), PATTERN);
        Period diff = Period.between(initialDate, finalDate);
        double interestRateDouble;
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
            interestRateDouble = format.parse(interestRate).doubleValue();
        } catch (ParseException e) {
            throw new LoanAccountRefreshException("Unable to parse the interest rate", e);
        }

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(accountType)
                                .withBalance(parseAmountInEuros(currentBalance))
                                .withInterestRate(interestRateDouble)
                                .setAmortized(parseAmountInEuros(amortizedAmount))
                                .setMonthlyAmortization(parseAmountInEuros(monthlyAmortization))
                                .setInitialDate(initialDate)
                                .setApplicants(applicants)
                                .setCoApplicant(applicants.size() > 1)
                                .setInitialBalance(parseAmountInEuros(initialBalance))
                                .setNumMonthsBound(diff.getMonths())
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(description)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN, accountNumber))
                                .build())
                .build();
    }
}
