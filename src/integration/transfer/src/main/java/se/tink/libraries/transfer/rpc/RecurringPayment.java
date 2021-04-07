package se.tink.libraries.transfer.rpc;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecurringPayment extends Transfer {

    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExecutionRule executionRule;
    private Integer dayOfExecution;
}
