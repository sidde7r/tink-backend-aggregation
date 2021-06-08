package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RemittanceInformationStructuredEntity {

    private final SkandiaConstants.ReferenceType referenceType;
    private final String reference;

    public static List<RemittanceInformationStructuredEntity> singleFrom(Payment payment) {
        return Collections.singletonList(
                new RemittanceInformationStructuredEntity(
                        PaymentProduct.from(payment).getReferenceType(),
                        payment.getRemittanceInformation().getValue()));
    }
}
