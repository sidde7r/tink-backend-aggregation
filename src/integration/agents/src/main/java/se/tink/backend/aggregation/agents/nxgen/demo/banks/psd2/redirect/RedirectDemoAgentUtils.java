package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect;

public class RedirectDemoAgentUtils {
    public static void throwIfFailStateProvider(String providerName) {
        if (providerName.matches(
                RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_FAILURE_CASE_REGEX)) {
            throw RedirectAuthenticationDemoAgentConstants.FAILED_CASE_EXCEPTION;
        } else if (providerName.matches(
                RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_CANCEL_CASE_REGEX)) {
            throw RedirectAuthenticationDemoAgentConstants.CANCELLED_CASE_EXCEPTION;
        }
    }

    public static void failPaymentIfFailStateProvider(String providerName) {
        if (providerName.matches(
                RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_PAYMENT_FAILED_CASE_REGEX)) {
            throw RedirectAuthenticationDemoAgentConstants.FAILED_CASE_EXCEPTION;
        } else if (providerName.matches(
                RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_PAYMENT_CANCEL_CASE_REGEX)) {
            throw RedirectAuthenticationDemoAgentConstants.CANCELLED_CASE_EXCEPTION;
        }
    }
}
