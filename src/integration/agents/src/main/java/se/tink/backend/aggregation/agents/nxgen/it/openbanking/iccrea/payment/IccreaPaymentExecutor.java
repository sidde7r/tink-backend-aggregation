package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaCredentialsAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaScaMethodSelectionStep;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.UserInteractions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.PaymentAuthorizationResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class IccreaPaymentExecutor extends CbiGlobePaymentExecutor {

    private final UserInteractions userInteractions;

    private final IccreaScaMethodSelectionStep scaMethodSelectionStep;
    private final IccreaCredentialsAuthenticationStep credentialsAuthenticationStep;

    public IccreaPaymentExecutor(
            CbiGlobePaymentApiClient paymentApiClient,
            SupplementalInformationController supplementalInformationController,
            CbiStorage storage,
            CbiGlobePaymentRequestBuilder paymentRequestBuilder,
            CbiGlobeAuthApiClient authApiClient,
            UserInteractions userInteractions,
            CbiUrlProvider urlProvider,
            Credentials credentials) {
        super(paymentApiClient, supplementalInformationController, storage, paymentRequestBuilder);
        this.userInteractions = userInteractions;
        credentialsAuthenticationStep =
                new IccreaCredentialsAuthenticationStep(
                        authApiClient, credentials, urlProvider.getRawPaymentUrl());
        scaMethodSelectionStep =
                new IccreaScaMethodSelectionStep(authApiClient, urlProvider.getRawPaymentUrl());
    }

    @Override
    protected void prepareAuthorization(CreatePaymentResponse createPaymentResponse) {
        PaymentAuthorizationResponse paymentAuthorizationResponse =
                credentialsAuthenticationStep.authenticate(
                        createPaymentResponse, PaymentAuthorizationResponse.class);
        scaMethodSelectionStep.pickScaMethod(paymentAuthorizationResponse);
        userInteractions.displayPromptAndWaitForAcceptance();
    }

    @Override
    protected void performRedirectIfNeeded() {
        // Iccrea does not require any redirects. Even with default implementation from super, we
        // wouldn't really execute anything. This is kept clear here just to be explicit about not
        // doing redirect.
    }
}
