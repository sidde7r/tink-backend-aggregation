package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.rpc;

import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostalePaymentSigner;

public class LaBanquePostalePaymentSignerTest {

    LaBanquePostalePaymentSigner laBanquePostalePaymentSigner = new LaBanquePostalePaymentSigner();

    @Test
    public void throwPaymentRejectedExceptionIfCallbackMissing() {
        Throwable thrown =
                catchThrowable(
                        () ->
                                laBanquePostalePaymentSigner.setPsuAuthenticationFactorOrThrow(
                                        new HashMap<>()));
        Assertions.assertThat(thrown).isInstanceOf(PaymentRejectedException.class);
    }

    @Test
    public void throwAuthenticationExceptionIfCallbackMissingAndAuthenticationError() {
        HashMap<String, String> callback = new HashMap<>();
        callback.put("error", "authentication_error");
        Throwable thrown =
                catchThrowable(
                        () ->
                                laBanquePostalePaymentSigner.setPsuAuthenticationFactorOrThrow(
                                        callback));
        Assertions.assertThat(thrown).isInstanceOf(PaymentAuthenticationException.class);
    }

    @Test
    public void setAuthenticationFactorIfCallbackPresent()
            throws PaymentRejectedException, PaymentAuthenticationException {
        HashMap<String, String> callback = new HashMap<>();
        callback.put("psuAuthenticationFactor", "authentication_factor");
        laBanquePostalePaymentSigner.setPsuAuthenticationFactorOrThrow(callback);
        Assertions.assertThat(laBanquePostalePaymentSigner.getPsuAuthenticationFactor())
                .isEqualTo("authentication_factor");
    }
}
