package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.converter;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domestic.converter.DomesticPaymentConverter;
import se.tink.libraries.payment.rpc.Payment;

public class HsbcDomesticPaymentConverter extends DomesticPaymentConverter {

    private final HsbcConverterBase converterBase = new HsbcConverterBase();

    @Override
    public RemittanceInformationDto getRemittanceInformationDto(Payment payment) {
        return converterBase.convertRemittanceInformationToDto(payment);
    }
}
