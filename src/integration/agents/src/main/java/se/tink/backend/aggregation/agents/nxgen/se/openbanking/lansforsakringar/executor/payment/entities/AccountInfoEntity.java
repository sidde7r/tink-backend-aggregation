package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.PaymentTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonObject
@Getter
public class AccountInfoEntity {

    private String bban;
    private List<String> allowedTransactionTypes;

    public boolean isDomesticTransferAllowed() {
        return allowedTransactionTypes.stream()
                .anyMatch(a -> a.equals(PaymentTypes.DOMESTIC_CREDIT_TRANSFERS_RESPONSE));
    }

    public boolean isDomesticGiroTransferAllowed() {
        return allowedTransactionTypes.stream()
                .anyMatch(a -> a.equals(PaymentTypes.DOMESTIC_GIROS_RESPONSE));
    }
}
