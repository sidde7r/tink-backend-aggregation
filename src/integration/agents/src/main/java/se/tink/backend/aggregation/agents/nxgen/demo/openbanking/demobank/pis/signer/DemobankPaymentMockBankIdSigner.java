package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ScaApproach;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class DemobankPaymentMockBankIdSigner implements DemobankPaymentSigner {

    private final DemobankPaymentApiClient apiClient;
    private final DemobankStorage apiStorage;

    private final BankIdAuthenticationController<String> authenticationController;
    private final Credentials credentials;
    private final PersistentStorage authStorage;

    @Override
    public void sign() throws PaymentAuthorizationException {
        try {
            authenticationController.authenticate(credentials);

            getAndStoreTokenFromAuthentication();

            apiClient.startPaymentAuthorisation(ScaApproach.BANK_ID);

            apiClient.signPayment();

        } catch (AuthenticationException | AuthorizationException e) {
            if (e.getError() instanceof BankIdError) {
                throw new PaymentAuthorizationException(e.getError().userMessage().get());
            }

        } catch (HttpResponseException | IllegalStateException e) {
            throw new PaymentAuthorizationException();
        }
    }

    private void getAndStoreTokenFromAuthentication() {
        OAuth2Token token =
                authStorage
                        .get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.OAUTH_TOKEN_NOT_FOUND));

        apiStorage.storeAccessToken(token);
    }
}
