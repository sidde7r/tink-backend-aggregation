package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas.payment;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants.ResponseValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.BnpParibasStatusParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;

public class BnpParibasStatusParserTest {

    BnpParibasStatusParser bnpParibasStatusParser = new BnpParibasStatusParser();

    @Test
    public void shouldThrowInsuffcientFundsException() {

        GetPaymentResponse getPaymentResponse =
                new GetPaymentResponse(
                        new PaymentEntity(
                                null,
                                ResponseValues.INSUFFICIENT_FUND_STATUS,
                                null,
                                null,
                                null,
                                null));

        // when
        PaymentException paymentException =
                bnpParibasStatusParser.parseErrorResponse(getPaymentResponse);

        Assertions.assertThat(paymentException.getLocalizedMessage())
                .isEqualTo(InsufficientFundsException.DEFAULT_MESSAGE);

        Assertions.assertThat(paymentException.getClass())
                .isEqualTo(InsufficientFundsException.class);
    }

    @Test
    public void shouldThrowPaymentRejectedException() {

        GetPaymentResponse getPaymentResponse =
                new GetPaymentResponse(new PaymentEntity(null, null, null, null, null, null));

        // when
        PaymentException paymentException =
                bnpParibasStatusParser.parseErrorResponse(getPaymentResponse);

        Assertions.assertThat(paymentException.getClass())
                .isEqualTo(PaymentRejectedException.class);
    }
}
