package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import com.google.common.base.Strings;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.Authorization.ScaMethod;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;

@Slf4j
@AllArgsConstructor
public class DkbPaymentAuthenticator implements PaymentAuthenticatorPreAuth {

    private final DkbAuthenticator dkbAuthenticator;

    private final DkbAuthApiClient dkbAuthApiClient;

    private final DkbSupplementalDataProvider supplementalDataProvider;

    private final Credentials credentials;

    @Override
    public void preAuthentication() {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        AuthResult result = dkbAuthenticator.authenticate1stFactor(username, password);
        dkbAuthenticator.processAuthenticationResult(result);
    }

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {
        Authorization authorization =
                dkbAuthApiClient.startPaymentAuthorization(scaLinks.getStartAuthorisation());
        Authorization consentAuthWithSelectedMethod =
                selectPaymentAuthorizationMethodIfNeeded(authorization);
        consentAuthWithSelectedMethod.checkIfChallengeDataIsAllowed();
        provide2ndFactorConsentAuthorization(consentAuthWithSelectedMethod);
    }

    private Authorization selectPaymentAuthorizationMethodIfNeeded(Authorization previousResult)
            throws AuthenticationException {
        if (!previousResult.isScaMethodSelectionRequired()) {
            return previousResult;
        }

        List<ScaMethod> allowedScaMethods = previousResult.getAllowedScaMethods();
        SelectableMethod selectedAuthMethod =
                supplementalDataProvider.selectAuthMethod(allowedScaMethods);
        Authorization consentAuthorization =
                dkbAuthApiClient.selectPaymentAuthorizationMethod(
                        previousResult.getLinks().getScaStatus(),
                        selectedAuthMethod.getIdentifier());
        dkbAuthenticator.setMissingAuthenticationType(selectedAuthMethod, consentAuthorization);
        return consentAuthorization;
    }

    private void provide2ndFactorConsentAuthorization(Authorization authorization)
            throws SupplementalInfoException, LoginException {
        String code =
                supplementalDataProvider.getTanCode(
                        authorization.getChosenScaMethod(),
                        authorization.getChallengeData().getData());
        dkbAuthApiClient.paymentAuthorization2ndFactor(
                authorization.getLinks().getAuthoriseTransaction(), code);
    }
}
