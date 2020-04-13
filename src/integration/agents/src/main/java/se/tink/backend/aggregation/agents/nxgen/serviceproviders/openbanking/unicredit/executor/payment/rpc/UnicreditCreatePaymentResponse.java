package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class UnicreditCreatePaymentResponse implements CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;

    @JsonProperty("_links")
    private LinksEntity links;

    @Override
    public String getPaymentId() {
        return paymentId;
    }

    @Override
    public String getScaRedirect() {
        return Optional.ofNullable(links)
                .map(LinksEntity::getScaRedirect)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        UnicreditConstants.ErrorMessages.MISSING_LINKS_ENTITY));
    }

    @Override
    public PaymentResponse toTinkPayment(
            String debtorAccountNumber, String creditorAccountNumber, PaymentType type) {

        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withCreditor(new Creditor(new IbanIdentifier(creditorAccountNumber)))
                        .withDebtor(new Debtor(new IbanIdentifier(debtorAccountNumber)))
                        .withStatus(
                                UnicreditPaymentStatus.fromString(transactionStatus)
                                        .getPaymentStatus())
                        .withType(type)
                        .build());
    }
}
