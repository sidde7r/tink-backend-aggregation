package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc.AuthorisationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class PostbankPaymentAuthenticator extends PostbankAuthenticationController
        implements PaymentAuthenticator {
    private Credentials credentials;

    public PostbankPaymentAuthenticator(
            Catalog catalog,
            SupplementalInformationController supplementalInformationController,
            PostbankAuthenticator authenticator,
            Credentials credentials) {
        super(catalog, supplementalInformationController, authenticator);
        this.credentials = credentials;
    }

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {
        validateReceivedCredentials(credentials);
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);
        AuthorisationResponse initValues =
                authenticator.startAuthorsation(
                        scaLinks.getStartAuthorisationWithEncryptedPsuAuthentication(),
                        username,
                        password);
        handleSca(initValues, username);
    }
}
