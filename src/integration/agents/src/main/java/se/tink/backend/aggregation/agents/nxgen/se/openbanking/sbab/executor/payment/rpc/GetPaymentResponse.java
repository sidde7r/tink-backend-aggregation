package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.entities.SignOptionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.enums.SBABPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {

    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("sign_options")
    private SignOptionsResponse signOptions;

    @JsonProperty("transfer_id")
    private String transferId;

    private String status;

    public PaymentResponse toTinkPaymentResponse(
            PaymentType paymentType, String debtorAccountNumber, String creditorAccountNumber) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(new Creditor(new SwedishIdentifier(creditorAccountNumber)))
                        .withDebtor(new Debtor(new SwedishIdentifier(debtorAccountNumber)))
                        .withUniqueId(referenceId)
                        .withStatus(SBABPaymentStatus.fromString(status).getPaymentStatus())
                        .withType(paymentType);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public String getSigningUrl() {
        return signOptions.getBankIdSignRedirectUrl();
    }
}
