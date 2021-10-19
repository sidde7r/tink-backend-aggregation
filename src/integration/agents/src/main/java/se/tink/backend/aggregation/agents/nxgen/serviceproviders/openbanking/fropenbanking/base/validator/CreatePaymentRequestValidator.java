package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;

public interface CreatePaymentRequestValidator {

    void validate(CreatePaymentRequest request) throws PaymentValidationException;
}
