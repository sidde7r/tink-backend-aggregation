package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class PaymentSignStatusResponse {

    private int status;
    private String statusName;

    public BankIdStatus getBankIdStatus() {
        return SkandiaBankenConstants.PAYMENT_SIGN_STATUS_MAPPER
                .translate(statusName.toLowerCase())
                .get();
    }
}
