package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentRequest {
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate requestedExecutionDate;
}
