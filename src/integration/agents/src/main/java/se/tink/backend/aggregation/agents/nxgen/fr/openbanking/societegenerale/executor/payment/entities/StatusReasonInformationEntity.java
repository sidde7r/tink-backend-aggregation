package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils.FrOpenBankingErrorMapper;

@Getter
@Setter
public class StatusReasonInformationEntity {
    private String value;

    public StatusReasonInformationEntity(String value) {
        this.value = value;
    }

    public void mapToError() throws PaymentException {
        throw FrOpenBankingErrorMapper.mapToError(value);
    }
}
