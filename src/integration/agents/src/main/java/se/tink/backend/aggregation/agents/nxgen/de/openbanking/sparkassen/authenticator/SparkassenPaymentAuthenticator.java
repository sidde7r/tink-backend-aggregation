package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class SparkassenPaymentAuthenticator extends SparkassenAuthenticator
        implements PaymentAuthenticator {

    public SparkassenPaymentAuthenticator(
            SparkassenApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            SparkassenStorage storage,
            Credentials credentials,
            Catalog catalog) {
        super(apiClient, supplementalInformationController, storage, credentials, catalog);
    }

    public void authenticatePayment(Credentials credentials, LinksEntity scaLinks) {
        validateInput(credentials);

        AuthorizationResponse initAuthorizationResponse =
                apiClient.initializeAuthorization(
                        scaLinks.getStartAuthorisationWithPsuAuthentication().getHref(),
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        authorisePayment(initAuthorizationResponse);
    }

    private void authorisePayment(AuthorizationResponse authResponseAfterLogin) {
        switch (authResponseAfterLogin.getScaStatus()) {
            case PSU_AUTHENTICATED:
                authorizeWithSelectedMethod(
                        pickMethodOutOfMultiplePossible(authResponseAfterLogin));
                break;
            case STARTED:
            case SCA_METHOD_SELECTED:
                authorizeWithSelectedMethod(authResponseAfterLogin);
                break;
            case EXEMPTED:
                // do nothing as SCA is exempted, authorization complete
                break;
            default:
                throw new IllegalStateException(
                        SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        }
    }
}
