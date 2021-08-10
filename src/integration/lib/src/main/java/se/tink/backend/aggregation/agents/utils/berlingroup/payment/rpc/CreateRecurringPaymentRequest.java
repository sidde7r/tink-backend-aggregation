package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecurringPaymentRequest extends CreatePaymentRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String frequency;
    private String executionRule;
    private String dayOfExecution;
}
