package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.Map;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PaymentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IccreaPaymentExecutor extends CbiGlobePaymentExecutor {

    private final UserInteractions userInteractions;
    private final ConsentManager consentManager;
    private final CbiUrlProvider urlProvider;

    public IccreaPaymentExecutor(
            CbiGlobeApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            SessionStorage sessionStorage,
            StrongAuthenticationState strongAuthenticationState,
            Provider provider,
            UserInteractions userInteractions,
            ConsentManager consentManager,
            CbiUrlProvider urlProvider) {
        super(
                apiClient,
                supplementalInformationHelper,
                sessionStorage,
                strongAuthenticationState,
                provider);
        this.userInteractions = userInteractions;
        this.consentManager = consentManager;
        this.urlProvider = urlProvider;
    }

    @Override
    protected void authorizePayment(CreatePaymentResponse createPaymentResponse) {
        PaymentAuthorizationResponse paymentAuthorizationResponse =
                consentManager.updatePsuCredentials(
                        createPaymentResponse.getPsuCredentials(),
                        new URL(
                                urlProvider.getRawPaymentUrl()
                                        + createPaymentResponse
                                                .getLinks()
                                                .getUpdatePsuAuthentication()
                                                .getHref()),
                        PaymentAuthorizationResponse.class);

        consentManager.changeAuthenticationMethod(
                paymentAuthorizationResponse,
                new URL(
                        urlProvider.getRawPaymentUrl()
                                + paymentAuthorizationResponse
                                        .getLinks()
                                        .getAuthorizeUrl()
                                        .getHref()));
        userInteractions.displayPromptAndWaitForAcceptance();
    }

    @Override
    protected Map<String, String> fetchSupplementalInfo(String redirectUrl) {
        return null;
    }
}
