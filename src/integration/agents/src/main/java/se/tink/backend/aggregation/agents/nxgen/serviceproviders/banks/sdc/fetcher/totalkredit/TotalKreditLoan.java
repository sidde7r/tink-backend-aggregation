package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import com.google.common.collect.ImmutableList;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
@EqualsAndHashCode
class TotalKreditLoan {
    private static final DateTimeFormatter DA_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Locale LOCALE_DA = new Locale("da");

    private String title;
    private String description;
    private TotalKreditLoanAmount amount;
    private List<TotalKreditLoanDetail> details = new ArrayList<>();

    private final NumberOfMonthsBoundCalculator numberOfMonthsBoundCalculator =
            new NumberOfMonthsBoundCalculator();

    public LoanAccount toTinkLoan(final String agreementId) {
        final String loanNumber = agreementId + " " + title;
        return LoanAccount.nxBuilder()
                .withLoanDetails(createLoanModule(loanNumber))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanNumber)
                                .withAccountNumber(loanNumber)
                                .withAccountName(Optional.ofNullable(title).orElse(""))
                                .addIdentifier(new DanishIdentifier(loanNumber))
                                .setProductName(title)
                                .build())
                .setApiIdentifier(loanNumber)
                .build();
    }

    private LoanModule createLoanModule(final String loanNumber) {
        return LoanModule.builder()
                .withType(Type.MORTGAGE)
                .withBalance(balance())
                .withInterestRate(interestRate())
                .setAmortized(amortized())
                .setInitialBalance(initialBalance())
                .setApplicants(Collections.emptyList())
                .setCoApplicant(false)
                .setLoanNumber(loanNumber)
                .setNextDayOfTermsChange(nextDayOfTermChange())
                .setSecurity(loanSecurity())
                .setNumMonthsBound(numOfMonthsBound())
                .setMonthlyAmortization(monthlyAmortization())
                .build();
    }

    private Double interestRate() {
        return findFirstMatchingDetail(d -> d.is("Rente"))
                .map(d -> AgentParsingUtils.parsePercentageFormInterest(d.value()))
                .orElse(0.0d);
    }

    private static Double parseToDoubleOrNull(final String value) {
        try {
            return NumberFormat.getNumberInstance(LOCALE_DA).parse(value).doubleValue();
        } catch (ParseException e) {
            log.error("Couldn't parse value {} using locale {}", value, LOCALE_DA);
            return null;
        }
    }

    private ExactCurrencyAmount initialBalance() {
        return detailValueToExactCurrencyAmount(d -> d.is("Hovedstol"));
    }

    private String loanSecurity() {
        return findFirstMatchingDetail(d -> d.is("Adresse"))
                .map(TotalKreditLoanDetail::value)
                .orElse(null);
    }

    private LocalDate nextDayOfTermChange() {
        return findFirstMatchingDetail(d -> d.is("Afdragsfrihed udløber"))
                .map(d -> parseToLocalDateOrNull(d.value()))
                .orElse(null);
    }

    private LocalDate parseToLocalDateOrNull(final String value) {
        try {
            return LocalDate.parse(value, DA_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error(
                    "Couldn't parse value {} using date formatter {}",
                    value,
                    DA_DATE_TIME_FORMATTER);
            return null;
        }
    }

    private ExactCurrencyAmount monthlyAmortization() {
        return detailValueToExactCurrencyAmount(d -> d.isSimilar("Ydelse"));
    }

    private ExactCurrencyAmount amortized() {
        String currency = currency();

        Optional<ExactCurrencyAmount> val1 =
                findFirstMatchingDetail(d -> d.is("Hovedstol"))
                        .map(d -> parseToDoubleOrNull(d.value()))
                        .map(d -> ExactCurrencyAmount.of(d, currency));

        ExactCurrencyAmount val2 =
                findFirstMatchingDetail(d -> d.is("Obligationsrestgæld"))
                        .map(d -> parseToDoubleOrNull(d.value()))
                        .map(d -> ExactCurrencyAmount.of(d, currency))
                        .orElse(ExactCurrencyAmount.zero(currency));

        return val1.map(d -> d.subtract(val2)).orElse(null);
    }

    private ExactCurrencyAmount balance() {
        return detailValueToExactCurrencyAmount(d -> d.is("Obligationsrestgæld"));
    }

    private ExactCurrencyAmount detailValueToExactCurrencyAmount(
            Predicate<TotalKreditLoanDetail> predicate) {
        Optional<Double> balance =
                findFirstMatchingDetail(predicate).map(d -> parseToDoubleOrNull(d.value()));

        String currency = currency();

        return balance.map(d -> ExactCurrencyAmount.of(d, currency)).orElse(null);
    }

    private Integer numOfMonthsBound() {
        return findFirstMatchingDetail(d -> d.is("Restløbetid"))
                .map(d -> numberOfMonthsBoundCalculator.calculate(d.value().replace("år", "år,")))
                .filter(v -> v > 0)
                .orElse(null);
    }

    private String currency() {
        return findFirstMatchingDetail(d -> d.is("Valuta"))
                .map(TotalKreditLoanDetail::value)
                .orElse("DKK");
    }

    private Optional<TotalKreditLoanDetail> findFirstMatchingDetail(
            Predicate<TotalKreditLoanDetail> predicate) {
        return details.stream().filter(predicate).findFirst();
    }
}

@Slf4j
class NumberOfMonthsBoundCalculator {

    private static final String MONTHS_IN_DANISH = "måneder";
    private static final String MONTHS_IN_ENGLISH = "months";

    private static final String YEARS_IN_DANISH = "år";
    private static final String YEARS_IN_ENGLISH = "years";

    private static final List<ToMonthCalculator> toMonthCalculators =
            ImmutableList.of(new MonthToMonthCalculator(), new YearToMonthCalculator());

    interface ToMonthCalculator {
        boolean canHandle(final String s);

        int extractValue(final String s);
    }

    static class MonthToMonthCalculator implements ToMonthCalculator {
        @Override
        public boolean canHandle(final String s) {
            return (s.contains(MONTHS_IN_DANISH) || s.contains(MONTHS_IN_ENGLISH))
                    && !(s.contains(YEARS_IN_DANISH) || s.contains(YEARS_IN_ENGLISH));
        }

        @Override
        public int extractValue(final String s) {
            return Integer.parseInt(
                    s.replace(MONTHS_IN_DANISH, "").replace(MONTHS_IN_ENGLISH, "").trim());
        }
    }

    static class YearToMonthCalculator implements ToMonthCalculator {
        @Override
        public boolean canHandle(final String s) {
            return (s.contains(YEARS_IN_DANISH) || s.contains(YEARS_IN_ENGLISH))
                    && !(s.contains(MONTHS_IN_DANISH) || s.contains(MONTHS_IN_ENGLISH));
        }

        @Override
        public int extractValue(final String s) {
            return 12
                    * Integer.parseInt(
                            s.replace(YEARS_IN_DANISH, "").replace(YEARS_IN_ENGLISH, "").trim());
        }
    }

    public int calculate(String s) {
        String[] parts = s.split(",");

        int monthSum = 0;

        for (String part : parts) {
            monthSum += calculateForPart(part);
        }

        return monthSum;
    }

    private int calculateForPart(final String part) {
        for (ToMonthCalculator calculator : toMonthCalculators) {
            if (calculator.canHandle(part)) {
                return calculator.extractValue(part);
            }
        }
        log.error("Found unknown part of maturity: {}", part);
        return 0;
    }
}
