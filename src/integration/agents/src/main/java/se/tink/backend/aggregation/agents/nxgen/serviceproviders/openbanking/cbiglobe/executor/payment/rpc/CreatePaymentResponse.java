package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.PisStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiCredentialsAuthenticatable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsDefinition;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentResponse implements CbiCredentialsAuthenticatable {

    private String transactionStatus;
    private String paymentId;

    private String psuAuthenticationStatus;
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    private PsuCredentialsDefinition psuCredentials;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(Payment tinkPayment) {
        // In case of BPM's special handling, payment is explicitly marked as signed
        // in case of RCVD, hence we dont want to again map transactionStatus
        // because it will be null and lead to CREATED.
        if (!(PaymentStatus.SIGNED.equals(tinkPayment.getStatus())
                && PisStatus.AUTHENTICATED.equalsIgnoreCase(psuAuthenticationStatus)
                && PisStatus.VERIFIED.equalsIgnoreCase(scaStatus))) {
            tinkPayment.setStatus(
                    CbiGlobePaymentStatus.mapToTinkPaymentStatus(
                            CbiGlobePaymentStatus.fromString(transactionStatus)));
        }
        if (paymentId != null) {
            tinkPayment.setUniqueId(paymentId); // bank Unique payment Id
        }
        return new PaymentResponse(tinkPayment);
    }

    @Override
    public String getUpdatePsuAuthenticationLink() {
        return links.getUpdatePsuAuthentication().getHref();
    }
}
