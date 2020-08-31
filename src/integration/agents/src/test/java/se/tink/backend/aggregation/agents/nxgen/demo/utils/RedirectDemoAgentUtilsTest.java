package se.tink.backend.aggregation.agents.nxgen.demo.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectDemoAgentUtils;

public class RedirectDemoAgentUtilsTest {

    @Rule public ExpectedException exception = ExpectedException.none();

    @Test
    public void testFailedCaseProvider() {
        exception.expect(TransferExecutionException.class);
        exception.expectMessage(
                "The transfer amount is larger than what is available on the account (test)");

        String providerName = "fr-test-open-banking-redirect-payment-failed";

        RedirectDemoAgentUtils.failPaymentIfFailStateProvider(providerName);
    }

    @Test
    public void testCancelledCaseProvider() {
        exception.expect(TransferExecutionException.class);
        exception.expectMessage("Cancel on payment signing (test)");

        String providerName = "fr-test-open-banking-redirect-payment-cancelled";

        RedirectDemoAgentUtils.failPaymentIfFailStateProvider(providerName);
    }
}
