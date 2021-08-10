package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RemittanceInformationStructuredEntity {

    private final SkandiaConstants.ReferenceType referenceType;
    private final String reference;

    public static List<RemittanceInformationStructuredEntity> singleFrom(Payment payment) {
        final SkandiaConstants.ReferenceType referenceType =
                PaymentProduct.from(payment).getReferenceType();

        final String reference =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(RemittanceInformation::getValue)
                        .orElse("");

        return Collections.singletonList(
                new RemittanceInformationStructuredEntity(referenceType, reference));
    }
}
