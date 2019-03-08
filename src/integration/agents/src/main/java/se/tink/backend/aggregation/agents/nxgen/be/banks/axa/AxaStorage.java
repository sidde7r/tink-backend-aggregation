package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.GenerateOtpChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.LogonResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.StoreRegistrationCdResponse;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.Digipass;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.models.OtpChallengeResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public final class AxaStorage {
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public AxaStorage(
            final SessionStorage sessionStorage, final PersistentStorage persistentStorage) {
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public void persistDigipass(@Nonnull Digipass response) {
        Preconditions.checkNotNull(response);
        persistentStorage.put(AxaConstants.Storage.DIGIPASS.name(), response.serialize());
    }

    public void persistClientInitialVectorInit(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.CLIENT_INITIAL_VECTOR_INIT.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistEncryptedPublicKeyAndNonce(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.ENCRYPTED_PUBLIC_KEY_AND_NONCE.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistServerInitialVector(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.SERVER_INITIAL_VECTOR.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistEncryptedNonces(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.ENCRYPTED_NONCES.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistEncryptedServerPublicKey(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.ENCRYPTED_SERVER_PUBLIC_KEY.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistXfad(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.XFAD.name(), Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistRegisterChallenge(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.REGISTER_CHALLENGE.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistSerialNo(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.SERIAL_NO.name(), Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistClientInitialVectorDecrypt(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.CLIENT_INITIAL_VECTOR_DECRYPT.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistEncryptedServerNonce(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.ENCRYPTED_SERVER_NONCE.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistDerivationCode(@Nonnull String value) {
        persistentStorage.put(
                AxaConstants.Storage.DERIVATION_CODE.name(),
                Preconditions.checkNotNullOrEmpty(value));
    }

    public void persistStoreRegistrationCdResponse(@Nonnull StoreRegistrationCdResponse response) {
        Preconditions.checkNotNull(response);
        persistentStorage.put(
                AxaConstants.Storage.STORE_REGISTRATION_CD_RESPONSE.name(),
                SerializationUtils.serializeToString(response));
    }

    public void persistOptChallengeResponse(@Nonnull GenerateOtpChallengeResponse response) {
        Preconditions.checkNotNull(response);
        persistentStorage.put(
                AxaConstants.Storage.GENERATE_OTP_CHALLENGE_RESPONSE.name(),
                SerializationUtils.serializeToString(response));
    }

    public void persistLogonResponse(@Nonnull LogonResponse response) {
        Preconditions.checkNotNull(response);
        persistentStorage.put(
                AxaConstants.Storage.LOGON_RESPONSE.name(),
                SerializationUtils.serializeToString(response));
    }

    public void persistDigiOtpChallengeResponse(@Nonnull OtpChallengeResponse response) {
        Preconditions.checkNotNull(response);
        persistentStorage.put(
                AxaConstants.Storage.DIGI_OTP_CHALLENGE_RESPONSE.name(),
                SerializationUtils.serializeToString(response));
    }

    public void persistDeviceId(@Nonnull UUID deviceId) {
        Preconditions.checkNotNull(deviceId);
        persistentStorage.put(AxaConstants.Storage.DEVICE_ID.name(), deviceId.toString());
    }

    public void persistBasicAuth(@Nonnull String basicAuth) {
        Preconditions.checkNotNull(basicAuth);
        persistentStorage.put(AxaConstants.Storage.BASIC_AUTH.name(), basicAuth);
    }

    public void persistLanguage(@Nonnull final String language) {
        Preconditions.checkNotNull(language);
        persistentStorage.put(AxaConstants.Storage.LANGUAGE.name(), language);
    }

    public void sessionStoreAccessToken(@Nonnull final String accessToken) {
        Preconditions.checkNotNull(accessToken);
        sessionStorage.put(AxaConstants.Storage.ACCESS_TOKEN.name(), accessToken);
    }

    public Optional<Digipass> getDigipass() {
        final String digipassSerialized =
                persistentStorage.get(AxaConstants.Storage.DIGIPASS.name());
        return Optional.ofNullable(digipassSerialized)
                .map(
                        string -> {
                            final Digipass digipass = new Digipass();
                            digipass.deserialize(digipassSerialized);
                            return digipass;
                        });
    }

    public Optional<String> getServerInitialVector() {
        return Optional.ofNullable(
                persistentStorage.get(AxaConstants.Storage.SERVER_INITIAL_VECTOR.name()));
    }

    public Optional<String> getEncryptedNonces() {
        return Optional.ofNullable(
                persistentStorage.get(AxaConstants.Storage.ENCRYPTED_NONCES.name()));
    }

    public Optional<String> getEncryptedServerPublicKey() {
        return Optional.ofNullable(
                persistentStorage.get(AxaConstants.Storage.ENCRYPTED_SERVER_PUBLIC_KEY.name()));
    }

    public Optional<String> getXfad() {
        return Optional.ofNullable(persistentStorage.get(AxaConstants.Storage.XFAD.name()));
    }

    public Optional<String> getRegisterChallenge() {
        return Optional.ofNullable(
                persistentStorage.get(AxaConstants.Storage.REGISTER_CHALLENGE.name()));
    }

    public Optional<String> getSerialNo() {
        return Optional.ofNullable(persistentStorage.get(AxaConstants.Storage.SERIAL_NO.name()));
    }

    public Optional<UUID> getDeviceId() {
        return Optional.ofNullable(persistentStorage.get(AxaConstants.Storage.DEVICE_ID.name()))
                .map(UUID::fromString);
    }

    public Optional<Integer> getCustomerId() {
        return Optional.ofNullable(
                        persistentStorage.get(AxaConstants.Storage.LOGON_RESPONSE.name()))
                .map(s -> SerializationUtils.deserializeFromString(s, LogonResponse.class))
                .map(LogonResponse::getCustomerId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Integer::parseInt);
    }

    public Optional<String> getBasicAuth() {
        return Optional.ofNullable(persistentStorage.get(AxaConstants.Storage.BASIC_AUTH.name()));
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(sessionStorage.get(AxaConstants.Storage.ACCESS_TOKEN.name()));
    }

    public Optional<String> getLanguage() {
        return Optional.ofNullable(persistentStorage.get(AxaConstants.Storage.LANGUAGE.name()));
    }

    public String serializePersistentStorage() {
        return SerializationUtils.serializeToString(persistentStorage);
    }
}
