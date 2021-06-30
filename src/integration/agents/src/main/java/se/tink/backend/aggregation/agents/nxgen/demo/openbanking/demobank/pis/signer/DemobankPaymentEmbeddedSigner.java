package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.signer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.DemobankPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.AuthorisationResponseDto;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.storage.DemobankStorage;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class DemobankPaymentEmbeddedSigner implements DemobankPaymentSigner {
    private static final String OTP_CODE_DESCRIPTION = "OTP Code";
    private static final String OTP_INPUT_FIELD = "otpinput";

    private final DemobankPaymentApiClient apiClient;
    private final DemobankStorage storage;
    private final SupplementalInformationController supplementalInformationController;
    private final Credentials credentials;

    @Override
    public void sign() throws PaymentAuthorizationException {
        OAuth2Token token =
                apiClient.loginUser(
                        credentials.getField(Key.USERNAME), credentials.getField(Key.PASSWORD));
        storage.storeAccessToken(token);

        final String authorizeUrl = storage.getEmbeddedAuthorizeUrl();
        AuthorisationResponseDto authorisationResponseDto =
                apiClient.startOtpAuthorisation(authorizeUrl);

        String otp = getOtpFromUser(authorisationResponseDto.getMessage());

        try {
            apiClient.signPaymentWithOtp(otp);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw new PaymentAuthorizationException(ErrorMessages.INVALID_OTP_CODE);
            }
        }
    }

    private String getOtpFromUser(String messageToDisplay) {
        return supplementalInformationController
                .askSupplementalInformationSync(
                        Field.builder()
                                .description(OTP_CODE_DESCRIPTION)
                                .helpText(messageToDisplay)
                                .immutable(true)
                                .masked(false)
                                .name(OTP_INPUT_FIELD)
                                .numeric(true)
                                .build())
                .get(OTP_INPUT_FIELD);
    }
}
