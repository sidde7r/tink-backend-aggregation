package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.FieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.ScaMethodFilter;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class SparkassenPaymentAuthenticator extends SparkassenAuthenticator
        implements PaymentAuthenticator {

    public SparkassenPaymentAuthenticator(
            SparkassenApiClient apiClient,
            SupplementalInformationController supplementalInformationController,
            SparkassenStorage storage,
            Credentials credentials,
            FieldBuilder fieldBuilder,
            ScaMethodFilter scaMethodFilter) {
        super(
                apiClient,
                supplementalInformationController,
                storage,
                credentials,
                fieldBuilder,
                scaMethodFilter);
    }

    public void authenticatePayment(LinksEntity scaLinks) {
        validateInput(credentials);
        AuthorizationResponse initAuthorizationResponse =
                apiClient.initializeAuthorization(
                        scaLinks.getStartAuthorisationWithPsuAuthentication(),
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD));

        authorize(initAuthorizationResponse);
    }
}
