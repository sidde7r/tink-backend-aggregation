package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator;

import com.google.common.io.BaseEncoding;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Data
public class UserContext {
    private final PersistentStorage persistentStorage;

    private String registrationSessionToken;
    private String accountNumber;
    private String accountAlias;
    private String registerToken;
    private boolean isUserPinSetRequired;

    public UserContext(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public void saveToPersistentStorage(String key, Object val) {
        this.persistentStorage.put(key, val);
    }

    public KeyPair getKeyPair() {
        return Optional.ofNullable(persistentStorage.get(Storage.KEY_PAIR))
                .map(SerializationUtils::deserializeKeyPair)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored key pair."));
    }

    public String getAppId() {
        return Optional.ofNullable(persistentStorage.get(Storage.APP_ID))
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored appId."));
    }

    public String getSecretApp() {
        return Optional.ofNullable(persistentStorage.get(Storage.SECRET_APP))
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored secretApp."));
    }

    public String getAppRegisterId() {
        return Optional.ofNullable(persistentStorage.get(Storage.APP_REGISTER_ID))
                .orElseThrow(
                        () -> new NoSuchElementException("Can't obtain stored appRegisterId."));
    }

    public String getAccessBasicToken() {
        return Optional.ofNullable(persistentStorage.get(Storage.ACCESS_BASIC_TOKEN))
                .orElseThrow(
                        () -> new NoSuchElementException("Can't obtain stored accessBasicToken."));
    }

    public String getAccessDataToken() {
        return Optional.ofNullable(persistentStorage.get(Storage.ACCESS_DATA_TOKEN))
                .orElseThrow(
                        () -> new NoSuchElementException("Can't obtain stored accessDataToken."));
    }

    public String getUserPin() {
        return Optional.ofNullable(persistentStorage.get(Storage.USER_PIN))
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored userPin."));
    }

    public RSAPublicKey getPubServerKey() {
        return Optional.ofNullable(persistentStorage.get(Storage.PUB_SERVER_KEY))
                .map(EncodingUtils::decodeBase64String)
                .map(RSA::getPubKeyFromBytes)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored pubServerKey."));
    }

    public byte[] getOtpSecretKey() {
        return Optional.ofNullable(persistentStorage.get(Storage.OTP_SECRET_KEY))
                .map(BaseEncoding.base32()::decode)
                .orElseThrow(() -> new NoSuchElementException("Can't obtain stored otpSecretKey."));
    }

    public boolean isManualAuthFinished() {
        return persistentStorage.get(Storage.MANUAL_AUTH_FINISH_FLAG, Boolean.class).orElse(false);
    }
}
