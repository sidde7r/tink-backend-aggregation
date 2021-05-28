package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingStatusParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

public class BoursoramaStatusParser extends FrOpenBankingStatusParser {

    @Override
    public PaymentException parseErrorResponse(GetPaymentResponse paymentResponse) {
        if (BoursoramaConstants.INSUFFICIENT_FUND_STATUS.equals(
                paymentResponse.getStatusReasonInformation())) {
            return new InsufficientFundsException(InsufficientFundsException.DEFAULT_MESSAGE);
        } else {
            return super.parseErrorResponse(paymentResponse);
        }
    }
}
