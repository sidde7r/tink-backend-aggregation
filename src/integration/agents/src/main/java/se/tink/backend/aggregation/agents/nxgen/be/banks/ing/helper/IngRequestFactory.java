package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticationContextEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.ClientInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CredentialsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.DescriptionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.ExtraEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.ItemEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SrpClientEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.RemoteEvidenceSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.SRP6ClientValues;

public class IngRequestFactory {

    private static final String REGISTER_USER = "Register User";

    private final IngStorage ingStorage;

    public IngRequestFactory(IngStorage ingStorage) {
        this.ingStorage = ingStorage;
    }

    public AuthenticateRequestEntity createAuthenticateRequestEntity(
            String identifyOtp, String ingId, String cardNr) {
        return AuthenticateRequestEntity.builder()
                .responseCode(identifyOtp)
                .ingId(ingId)
                .cardId(cardNr)
                .encryptedId("")
                .authenticationContext(
                        AuthenticationContextEntity.builder()
                                .requiredLevelOfAssurance(1)
                                .clientId(IngConstants.CLIENT_ID)
                                .identifyeeType("customer")
                                .scopes(new String[] {"personal_data"})
                                .build())
                .keyId("")
                .build();
    }

    public CreateEnrollmentRequestEntity createEnrollmentRequestEntity(
            String mobileAppId,
            String mpinVerifier,
            String mpinSalt,
            String deviceVerifier,
            String deviceSalt) {

        String pinningKey =
                Base64.getEncoder()
                        .encodeToString(ingStorage.getEnrollPinningPublicKey().getEncoded());

        String signingKey =
                Base64.getEncoder()
                        .encodeToString(ingStorage.getEnrollSigningPublicKey().getEncoded());

        return CreateEnrollmentRequestEntity.builder()
                .devicePinningKey(pinningKey)
                .mobileAppId(mobileAppId)
                .signingKey(signingKey)
                .credentials(
                        Arrays.asList(
                                CredentialsEntity.builder()
                                        .means("MPIN")
                                        .verifier(mpinVerifier)
                                        .salt(mpinSalt)
                                        .build(),
                                CredentialsEntity.builder()
                                        .means("DEVICE")
                                        .verifier(deviceVerifier)
                                        .salt(deviceSalt)
                                        .build()))
                .description(
                        DescriptionEntity.builder()
                                .language("EN")
                                .title(null)
                                .summary200(REGISTER_USER)
                                .summary80(REGISTER_USER)
                                .items(
                                        Collections.singletonList(
                                                new ItemEntity("TextItem", REGISTER_USER)))
                                .summary(REGISTER_USER)
                                .build())
                .clientInfo(
                        ClientInfoEntity.builder()
                                .deviceModel(Headers.DEVICE_MODEL_VALUE)
                                .clientId(IngConstants.CLIENT_ID)
                                .build())
                .build();
    }

    public SignRequestEntity createSignRequest(String responseCode) {
        String challenge = ingStorage.getChallenge();
        String basketId = ingStorage.getBasketId();

        return SignRequestEntity.builder()
                .challenge(challenge)
                .basketId(basketId)
                .responseCode(responseCode)
                .build();
    }

    public RemoteEvidenceSessionRequest createRemoteEvidenceSessionRequest(
            SRP6ClientValues srp6ClientValues,
            String clientEvidenceMessageSignature,
            int levelOfAssurance) {
        return RemoteEvidenceSessionRequest.builder()
                .srp(
                        SrpClientEntity.builder()
                                .clientEvidenceMessage(srp6ClientValues.getEvidenceString())
                                .clientPublicValue(srp6ClientValues.getPublicValueString())
                                .clientEvidenceMessageSignature(clientEvidenceMessageSignature)
                                .build())
                .extra(
                        ExtraEntity.builder()
                                .requiredLevelOfAssurance(levelOfAssurance)
                                .clientId(IngConstants.CLIENT_ID)
                                .identifyeeType("customer")
                                .scopes(Collections.singletonList("personal_data"))
                                .build())
                .build();
    }
}
