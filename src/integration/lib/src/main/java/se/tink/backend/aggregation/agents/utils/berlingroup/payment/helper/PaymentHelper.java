package se.tink.backend.aggregation.agents.utils.berlingroup.payment.helper;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class PaymentHelper {

    public static LocalDate getBusinessDay(int daysToAdd) {
        LocalDate startDate = LocalDate.now().plusDays(daysToAdd);
        int shift = 0;
        if (startDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
            shift = 2;
        } else if (startDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            shift = 1;
        }
        return startDate.plusDays(shift);
    }
}
