package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class IngCreateRecurringPaymentRequest extends IngCreatePaymentRequest {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate endDate;

    private final String frequency;
    private final String dayOfExecution;
}
