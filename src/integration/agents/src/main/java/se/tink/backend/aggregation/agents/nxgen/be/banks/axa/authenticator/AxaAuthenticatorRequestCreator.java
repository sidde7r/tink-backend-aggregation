package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import java.security.KeyPair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AnonymousInvokeBindRequestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ParamsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AnonymousInvokeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.AssertFormRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BindRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils.AxaCryptoUtil;

class AxaAuthenticatorRequestCreator {

    private static final String CONFIRMATION_PAYLOAD =
            "{\"params\":"
                    + "{\"title\":\"Confirm\","
                    + "\"text\":\"Register fingerprint\","
                    + "\"continue_button_text\":\"Yes\","
                    + "\"cancel_button_text\":\"No\","
                    + "\"parameters\":[]},"
                    + "\"user_input\":\"No\"}";

    private final AxaStorage storage;

    AxaAuthenticatorRequestCreator(AxaStorage storage) {
        this.storage = storage;
    }

    AnonymousInvokeRequest createAnonymousInvokeRequest() {
        AnonymousInvokeBindRequestData data =
                AnonymousInvokeBindRequestData.createAnonymousInvokeData(
                        storage.getDeviceId(),
                        storage.getDeviceName(),
                        storage.getParamsSessionId());
        return new AnonymousInvokeRequest(data);
    }

    AssertFormRequest createUCRAssertFormRequest() {
        return AssertFormRequest.createUCRAssertFormRequest(
                storage.getAssertionId(), storage.getFch(), storage.getUid());
    }

    AssertFormRequest createCardNumberAssertFormRequest() {
        return AssertFormRequest.createCardNumberAssertFormRequest(
                storage.getAssertionId(),
                storage.getFch(),
                storage.getCardNumber(),
                storage.getUid());
    }

    AssertFormRequest createAssertAuthenticationRequest() {
        return AssertFormRequest.createOtpAssertAuthenticationRequest(
                storage.getAssertionId(),
                storage.getFch(),
                storage.getCardNumber(),
                storage.getCardReaderResponse(),
                storage.getUid());
    }

    BindRequest createBindRequest() {
        AnonymousInvokeBindRequestData data =
                AnonymousInvokeBindRequestData.createBindData(
                        storage.getDeviceId(),
                        storage.getDeviceName(),
                        createBindParams(),
                        storage.getEncodedRSAPublicKey(),
                        storage.getRequestSignatureECPublicKey());
        return new BindRequest(data, storage.getUid());
    }

    AssertFormRequest createProfileNameAssertFormRequest() {
        return AssertFormRequest.createProfileNameAssertFormRequest(
                storage.getAssertionId(),
                storage.getFch(),
                storage.getUid(),
                storage.getFirstName());
    }

    AssertFormRequest createAssertRegistrationRequest() {
        final KeyPair keyPair = storage.getChallengeSignECKeyPair();
        final String signedFch =
                AxaCryptoUtil.createSignedFch(keyPair, storage.getFch(), storage.getAssertionId());
        final String ecRawPublicKey = AxaCryptoUtil.getRawEcPublicKey(keyPair);
        return AssertFormRequest.createAssertRegistrationRequest(
                storage.getAssertionId(), signedFch, ecRawPublicKey, storage.getUid());
    }

    AssertFormRequest createAssertConfirmationRequest() {
        final KeyPair keyPair = storage.getRequestSignatureECKeyPair();
        String signedPayload = AxaCryptoUtil.createSignedData(keyPair, CONFIRMATION_PAYLOAD);
        return AssertFormRequest.createAssertConfirmationRequest(
                storage.getAssertionId(),
                storage.getFch(),
                CONFIRMATION_PAYLOAD,
                signedPayload,
                storage.getUid());
    }

    LoginRequest createLoginRequest() {
        AnonymousInvokeBindRequestData data =
                AnonymousInvokeBindRequestData.createLoginData(
                        storage.getDeviceId(), storage.getDeviceName(), createLoginParams());
        return new LoginRequest(data, storage.getUid());
    }

    AssertFormRequest createAssertPinAuthenticationRequest() {
        return AssertFormRequest.createAssertAuthenticationRequest(
                storage.getAssertionId(), createSignedFch(), storage.getUid());
    }

    private ParamsEntity createBindParams() {
        return new ParamsEntity()
                .withAxaCardNumber(storage.getCardNumber())
                .withPanSequenceNumber(storage.getPanSequenceNumber())
                .withSessionId(storage.getParamsSessionId())
                .withTransactionType("mobile-registration")
                .withTransmitTicketId(storage.getTransmitTicketId());
    }

    private ParamsEntity createLoginParams() {
        return new ParamsEntity()
                .withAid("mobile")
                .withSessionId(storage.getParamsSessionId())
                .withTransactionType("mobile-login");
    }

    private String createSignedFch() {
        final KeyPair keyPair = storage.getChallengeSignECKeyPair();
        return AxaCryptoUtil.createSignedFch(keyPair, storage.getFch(), storage.getAssertionId());
    }
}
