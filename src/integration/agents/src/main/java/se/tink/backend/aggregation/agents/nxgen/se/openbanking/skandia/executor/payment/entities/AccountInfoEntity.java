package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@RequiredArgsConstructor
@JsonObject
@Getter
public class AccountInfoEntity {

    private final String bban;
    private final List<String> allowedTransactionTypes;

    public boolean isDomesticTransferAllowed() {
        return allowedTransactionTypes.stream()
                .anyMatch(a -> a.equals(PaymentTypes.DOMESTIC_CREDIT_TRANSFERS_RESPONSE));
    }

    public boolean isDomesticGiroTransferAllowed() {
        return allowedTransactionTypes.stream()
                .anyMatch(a -> a.equals(PaymentTypes.DOMESTIC_GIROS_RESPONSE));
    }
}
