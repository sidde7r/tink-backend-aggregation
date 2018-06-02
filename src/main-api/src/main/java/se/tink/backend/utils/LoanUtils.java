package se.tink.backend.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.core.LoanTimeline;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.InfoLoanEvent;
import se.tink.backend.core.InterestRateDecreaseLoanEvent;
import se.tink.backend.core.InterestRateIncreaseLoanEvent;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.Loan;
import se.tink.backend.core.LoanEvent;
import se.tink.backend.core.TemporalValue;

public class LoanUtils {


    public static List<TemporalValue<Loan>> convertLoansToTemporalValues(List<Loan> loanStates) {
        loanStates.sort(Loan::compareTo); // Sort the values

        return loanStates.stream().map(
                loan -> new TemporalValue<>(UUIDUtils.UUIDToDate(loan.getId()), loan))
                .collect(Collectors.toList());
    }

    public static List<Loan> filterForMonthlyPeriods(List<Loan> loanStates) {
        List<Loan> filtered = Lists.newArrayList();
        loanStates.sort(Loan::compareTo);

        filtered.add(loanStates.get(0));
        LocalDate previousDate = UUIDUtils.UUIDToDate(filtered.get(0).getId())
                .toInstant().atZone(ZoneId.of("CET")).toLocalDate();

        for (int i = 1; i < loanStates.size(); i++) {
            Loan loanState = loanStates.get(i);
            LocalDate currentDate = LocalDate.from(UUIDUtils.UUIDToDate(loanState.getId())
                    .toInstant().atZone(ZoneId.of("CET")).toLocalDate());
            Period p = Period.between(previousDate, currentDate);

            if (p.getMonths() == 0) {
                filtered.set(filtered.size() - 1, loanState);
            } else {
                filtered.add(loanState);
            }
            previousDate = currentDate;
        }
        return filtered;
    }

    public static ListMultimap<YearMonth,ValueWeight> convertLoanTimelinesToTimelineMap(List<LoanTimeline> loanTimelines) {
        ListMultimap<YearMonth, ValueWeight> timelineMap = ArrayListMultimap.create();
        for (LoanTimeline loanTimeline : loanTimelines) {
            YearMonth start = null;
            List<TemporalValue<Double>> balanceTimeline = loanTimeline.getBalanceTimeline();
            List<TemporalValue<Double>> interestTimeline = loanTimeline.getInterestRateTimeline();
            for (int i = 0; i < balanceTimeline.size(); i++) {
                if (i == 0) {
                    start = YearMonth.from(balanceTimeline.get(i).getDate().toInstant().atZone(ZoneId.of("CET")));
                }
                timelineMap.put(start.plusMonths(i), new ValueWeight(interestTimeline.get(i).getValue(), balanceTimeline.get(i).getValue()));
            }
        }

        return timelineMap;
    }

    public static List<KVPair<String, Double>> timelineMapWeightedAverage(ListMultimap<YearMonth, ValueWeight> timelineMap) {
        List<KVPair<String, Double>> weightedAverages = Lists.newArrayList();

        for (YearMonth month : timelineMap.asMap().keySet()) {
            weightedAverages.add(new KVPair<>(month.toString(), MathUtils.weightedAverage(timelineMap.get(month))));
        }

        return weightedAverages;
    }

    public static Map<String, List<Loan>> filterMapByLoanType(Map<String, List<Loan>> loanMap, Loan.Type loanType) {
        return Maps.filterValues(loanMap, loans -> Objects.equals(loans.get(0).getType(), loanType));
    }

    public static List<LoanEvent> createLoanEvents(List<Loan> loanStates, String userLocale) {

        List<LoanEvent> events = Lists.newArrayList();

        Loan previous = loanStates.get(0);
        String accountId = previous.getAccountId().toString();
        LoanEvent event = new InfoLoanEvent(
                previous.getAccountId().toString(),
                previous.getUpdated(),
                previous.getType(),
                previous.getNextDayOfTermsChange(),
                previous.getProviderName(),
                previous.getCredentialsId().toString(),
                previous.getName(),
                previous.getInterest(),
                previous.getBalance(),
                "Loan terms change date",
                Catalog.getCatalog(userLocale)
        );
        events.add(event);

        for (int i = 1; i < loanStates.size(); i++) {
            Loan loanState = loanStates.get(i);

            if (!Doubles.fuzzyEquals(loanState.getInterest(), previous.getInterest(), 0.000001)) {
                Double currentInterest = loanState.getInterest();
                Double previousInterest = previous.getInterest();

                event = null;
                // NOTE: We do the checks in the following order since the first loan in the list is the most recent
                if (currentInterest < previousInterest) {
                    event = new InterestRateIncreaseLoanEvent(
                            accountId,
                            loanState.getUpdated(),
                            loanState.getType(),
                            loanState.getNextDayOfTermsChange(),
                            loanState.getProviderName(),
                            loanState.getCredentialsId().toString(),
                            previousInterest - currentInterest,
                            currentInterest,
                            loanState.getBalance(),
                            Catalog.getCatalog(userLocale)
                    );
                }
                else if (currentInterest > previousInterest) {
                    event = new InterestRateDecreaseLoanEvent(
                            accountId,
                            loanState.getUpdated(),
                            loanState.getType(),
                            loanState.getNextDayOfTermsChange(),
                            loanState.getProviderName(),
                            loanState.getCredentialsId().toString(),
                            previousInterest - currentInterest,
                            currentInterest,
                            loanState.getBalance(),
                            Catalog.getCatalog(userLocale)
                    );
                }
                // NOTE: Should we add an InfoLoanEvent for currentInterest == previousInterest?
                // (Only if that implies other change)

                events.add(event);
            }

            previous = loanState;
        }

        return events;
    }

    public static Double sumBalance(List<Loan> loans){
        return loans.stream().mapToDouble(Loan::getBalance).sum();
    }

    public static Double interestRateWeightedAverage(List<Loan> loans){
        List<ValueWeight> valueWeights = loans.stream().map(
                loan -> new ValueWeight(loan.getInterest(), loan.getBalance())
        ).collect(Collectors.toList());
        return MathUtils.weightedAverage(valueWeights);
    }

    public static Double interestWeightedAverage(List<LoanEvent> loanEvents) {
        List<ValueWeight> valueWeights = loanEvents.stream()
                .map(loanEvent -> new ValueWeight(
                        loanEvent.getInterest() + loanEvent.getInterestRateChange(),
                        loanEvent.getBalance()))
                .collect(Collectors.toList());
        return MathUtils.weightedAverage(valueWeights);
    }

    public static Double interestRateChangeWeightedAverage(List<LoanEvent> loanEvents) {
        List<ValueWeight> valueWeights = loanEvents.stream()
                .map(loanEvent -> new ValueWeight(loanEvent.getInterestRateChange(),
                        loanEvent.getBalance()))
                .collect(Collectors.toList());
        return MathUtils.weightedAverage(valueWeights);
    }



    public static String generateNotificationKey(String message, Date date) {
        return message + "." + ThreadSafeDateFormat.FORMATTER_SECONDS.format(date);
    }
}
