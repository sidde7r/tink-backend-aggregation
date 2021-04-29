package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.loan.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.LOCAL_DATE_PATTERN;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils.parseAmountInEuros;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
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

        LocalDate initialDate = LocalDate.parse(getStartDate(), LOCAL_DATE_PATTERN);
        LocalDate finalDate = LocalDate.parse(getEndDate(), LOCAL_DATE_PATTERN);
        Period diff = Period.between(initialDate, finalDate);

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(accountType)
                                .withBalance(parseAmountInEuros(currentBalance))
                                .withInterestRate(
                                        Double.parseDouble(
                                                interestRate.replace(",", ".").replace("\"", "")))
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
