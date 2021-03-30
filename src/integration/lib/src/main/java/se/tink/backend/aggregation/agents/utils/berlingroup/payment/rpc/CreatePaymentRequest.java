package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentRequest {
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requestedExecutionDate;
}
