package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.converter.RequiredReferenceRemittanceInfoConverterBase;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.libraries.payment.rpc.Payment;

public class RequiredReferenceRemittanceInfoDomesticPaymentConverter
        extends DomesticPaymentConverter {

    private final RequiredReferenceRemittanceInfoConverterBase converterBase =
            new RequiredReferenceRemittanceInfoConverterBase();

    @Override
    public RemittanceInformationDto getRemittanceInformationDto(Payment payment) {
        return converterBase.convertRemittanceInformationToDto(payment);
    }
}
