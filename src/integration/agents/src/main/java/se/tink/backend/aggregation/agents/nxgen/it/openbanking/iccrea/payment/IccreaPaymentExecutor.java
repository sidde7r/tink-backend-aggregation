package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.payment;

import java.util.Collections;
import java.util.Map;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaCredentialsAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaScaMethodSelectionStep;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.UserInteractions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.PaymentAuthorizationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class IccreaPaymentExecutor extends CbiGlobePaymentExecutor {

    private final UserInteractions userInteractions;

    private final IccreaScaMethodSelectionStep scaMethodSelectionStep;
    private final IccreaCredentialsAuthenticationStep credentialsAuthenticationStep;

    public IccreaPaymentExecutor(
            CbiGlobeAuthApiClient authApiClient,
            CbiGlobePaymentApiClient paymentApiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            Provider provider,
            UserInteractions userInteractions,
            CbiUrlProvider urlProvider,
            Credentials credentials,
            CbiStorage storage) {
        super(
                authApiClient,
                paymentApiClient,
                supplementalInformationHelper,
                strongAuthenticationState,
                provider,
                storage);
        this.userInteractions = userInteractions;
        credentialsAuthenticationStep =
                new IccreaCredentialsAuthenticationStep(
                        authApiClient, credentials, urlProvider.getRawPaymentUrl());
        scaMethodSelectionStep =
                new IccreaScaMethodSelectionStep(authApiClient, urlProvider.getRawPaymentUrl());
    }

    @Override
    protected void authorizePayment(CreatePaymentResponse createPaymentResponse) {
        PaymentAuthorizationResponse paymentAuthorizationResponse =
                credentialsAuthenticationStep.authenticate(
                        createPaymentResponse, PaymentAuthorizationResponse.class);
        scaMethodSelectionStep.pickScaMethod(paymentAuthorizationResponse);
        userInteractions.displayPromptAndWaitForAcceptance();
    }

    @Override
    // This doesn't make much sense. It is only here to fit the CBI payment executor, which in
    // return makes the executor a bit weird.
    protected Map<String, String> fetchSupplementalInfo(String redirectUrl) {
        return Collections.emptyMap();
    }
}
