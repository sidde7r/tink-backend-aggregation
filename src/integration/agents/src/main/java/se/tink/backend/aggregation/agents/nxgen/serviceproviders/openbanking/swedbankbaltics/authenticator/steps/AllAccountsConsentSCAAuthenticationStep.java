package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.helper.SCAAuthenticationHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class AllAccountsConsentSCAAuthenticationStep implements AuthenticationStep {

    private final SwedbankBalticsApiClient apiClient;
    private final StepDataStorage stepDataStorage;
    private final PersistentStorage persistentStorage;
    private final SwedbankBalticsAuthenticator authenticator;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        Optional<ConsentResponse> consentResponse =
                stepDataStorage.getConsentResponseForAllAccounts();
        SCAAuthenticationHelper scaAuthenticationHelper =
                new SCAAuthenticationHelper(
                        apiClient, stepDataStorage, persistentStorage, authenticator);
        if (consentResponse.isPresent()) {
            scaAuthenticationHelper.scaAuthentication(consentResponse.get());
            return AuthenticationStepResponse.executeNextStep();

        } else {
            log.error(
                    "Could not find consent response during {} step",
                    Steps.ALL_ACCOUNTS_CONSENT_AUTH);
            throw new IllegalStateException("Could not find consent response");
        }
    }

    @Override
    public String getIdentifier() {
        return Steps.ALL_ACCOUNTS_CONSENT_AUTH;
    }
}
