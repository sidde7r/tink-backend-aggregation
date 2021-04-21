package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.enums.BankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.enums.PaymentStatus;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentResponse {

    private String transactionStatus;

    public PaymentStatus getPaymentStatus() {
        return BankPaymentStatus.fromString(transactionStatus).getPaymentStatus();
    }
}
