package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.encryption.LibTFA;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.ApplicationEntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.EntryPointResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.UserCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.HandshakeRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.HandshakeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ServerProfileRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ServerProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.rpc.auto.ValidateSignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.ChallengeResponseValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.ServerProfileValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators.ValidateSignatureValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.agents.rpc.Credentials;

public class HandelsbankenAutoAuthenticator implements AutoAuthenticator {
    private final HandelsbankenApiClient client;
    private final HandelsbankenPersistentStorage persistentStorage;
    private final Credentials credentials;
    private final HandelsbankenSessionStorage sessionStorage;
    private final HandelsbankenConfiguration configuration;

    public HandelsbankenAutoAuthenticator(HandelsbankenApiClient client,
            HandelsbankenPersistentStorage persistentStorage, Credentials credentials,
            HandelsbankenSessionStorage sessionStorage,
            HandelsbankenConfiguration configuration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        LibTFA tfa = persistentStorage.getTfa(credentials);

        EntryPointResponse entrypoint = client.fetchEntryPoint();

        HandshakeResponse handshake = client.handshake(entrypoint,
                new HandshakeRequest()
                        .setCnonce(tfa.generateNewClientNonce())
                        .setHandshakePubKey(tfa.generateHandshakeAndGetPublicKey())
        );

        ServerProfileResponse serverProfile = client.serverProfile(handshake,
                new ServerProfileRequest()
                        .setEncUserCredentials(tfa.generateEncUserCredentials(handshake, UserCredentialsRequest.create(
                                credentials.getField(Field.Key.USERNAME),
                                credentials.getField(Field.Key.PASSWORD)
                        )))
                        .setProfileId(persistentStorage.getProfileId())
        );

        new ServerProfileValidator(serverProfile).validate();

        ChallengeResponse challenge = client.challenge(serverProfile,
                new ChallengeRequest()
                        .setCnonce(tfa.generateNewClientNonce())
        );

        new ChallengeResponseValidator(credentials, challenge).validate();

        ValidateSignatureResponse validateSignature = client.validateSignature(
                challenge,
                new ValidateSignatureRequest()
                        .setPdeviceSignContainer(tfa.generatePDeviceSignContainer(challenge))
        );

        new ValidateSignatureValidator(credentials, validateSignature).validate();

        AuthorizeResponse authorize = configuration.toAuthorized(validateSignature, credentials, client);

        ApplicationEntryPointResponse applicationEntryPoint = client.applicationEntryPoint(authorize);

        persistentStorage.persist(authorize);
        sessionStorage.persist(applicationEntryPoint);
    }
}
