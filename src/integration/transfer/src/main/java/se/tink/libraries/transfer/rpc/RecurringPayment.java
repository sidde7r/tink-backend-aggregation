package se.tink.libraries.transfer.rpc;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Getter
@Setter
public class RecurringPayment extends Transfer {

    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private ExecutionRule executionRule;
    @Deprecated private Integer dayOfExecution;
    private Integer dayOfMonth;
    private DayOfWeek dayOfWeek;

    public void setDestination(
            AccountIdentifierType destinationType, String destinationId, String creditorName) {
        if (isNullOrEmpty(destinationId)) {
            return;
        }
        AccountIdentifier destination = AccountIdentifier.create(destinationType, destinationId);
        destination.setName(creditorName);
        setDestination(destination);
    }

    public ExecutionRule getExecutionRule() {
        return executionRule == ExecutionRule.UNSPECIFIED ? null : executionRule;
    }
}
