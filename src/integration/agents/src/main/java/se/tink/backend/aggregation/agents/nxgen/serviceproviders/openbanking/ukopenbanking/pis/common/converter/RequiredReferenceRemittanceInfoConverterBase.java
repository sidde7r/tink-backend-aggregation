package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class RequiredReferenceRemittanceInfoConverterBase {

    public RemittanceInformationDto convertRemittanceInformationToDto(Payment payment) {
        final String value =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(RemittanceInformation::getValue)
                        .orElse("");

        final RemittanceInformationType type =
                Optional.ofNullable(payment.getRemittanceInformation())
                        .map(RemittanceInformation::getType)
                        .orElse(null);

        if (type == RemittanceInformationType.REFERENCE) {
            return RemittanceInformationDto.builder().reference(value).build();
        } else {
            return RemittanceInformationDto.builder().unstructured(value).reference(value).build();
        }
    }
}
