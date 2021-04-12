package se.tink.backend.aggregation.nxgen.core.transaction;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionDates {
    private final List<TransactionDate> dates;

    private TransactionDates(List<TransactionDate> dates) {
        this.dates = dates;
    }

    public List<se.tink.backend.aggregation.agents.models.TransactionDate> toSystemModel() {
        return this.dates.stream().map(TransactionDate::toSystemModel).collect(Collectors.toList());
    }

    public List<TransactionDate> getDates() {
        return dates;
    }

    public static TransactionDates.Builder builder() {
        return new TransactionDates.Builder();
    }

    public static class Builder {
        private final Map<TransactionDateType, AvailableDateInformation> dates;

        private Builder() {
            // Using LinkedHashMap to maintain order, we'll get flaky wiremock tests otherwise.
            this.dates = new LinkedHashMap<>();
        }

        public TransactionDates.Builder setValueDate(AvailableDateInformation dateInformation) {
            dates.put(TransactionDateType.VALUE_DATE, dateInformation);
            return this;
        }

        public TransactionDates.Builder setBookingDate(AvailableDateInformation dateInformation) {
            dates.put(TransactionDateType.BOOKING_DATE, dateInformation);
            return this;
        }

        public TransactionDates.Builder setExecutionDate(AvailableDateInformation dateInformation) {
            dates.put(TransactionDateType.EXECUTION_DATE, dateInformation);
            return this;
        }

        public TransactionDates build() {
            return new TransactionDates(
                    this.dates.entrySet().stream()
                            .map(
                                    entry ->
                                            TransactionDate.builder()
                                                    .type(entry.getKey())
                                                    .value(entry.getValue())
                                                    .build())
                            .collect(Collectors.toList()));
        }
    }
}
