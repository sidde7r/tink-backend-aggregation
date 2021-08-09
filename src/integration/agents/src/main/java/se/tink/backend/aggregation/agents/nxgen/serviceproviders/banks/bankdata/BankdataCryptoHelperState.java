package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys.IV_STORAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys.KEY_PAIR_ID_STORAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys.PRIVATE_KEY_STORAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys.PUBLIC_KEY_STORAGE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.StorageKeys.SESSION_KEY_STORAGE;

import java.security.KeyPair;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.storage.Storage;

@Getter
@RequiredArgsConstructor
public class BankdataCryptoHelperState {

    private final String keyPairId;
    private final KeyPair keyPair;
    private final byte[] sessionKey;
    private final byte[] iv;

    public void saveInStorage(final Storage storage) {
        storage.put(KEY_PAIR_ID_STORAGE, keyPairId);
        storage.put(
                PUBLIC_KEY_STORAGE, Base64.encodeBase64String(keyPair.getPublic().getEncoded()));
        storage.put(
                PRIVATE_KEY_STORAGE, Base64.encodeBase64String(keyPair.getPrivate().getEncoded()));
        storage.put(SESSION_KEY_STORAGE, Base64.encodeBase64String(sessionKey));
        storage.put(IV_STORAGE, Base64.encodeBase64String(iv));
    }

    public static Optional<BankdataCryptoHelperState> loadFromStorage(Storage storage) {
        if (!canLoad(storage)) {
            return Optional.empty();
        }

        String keyPairId = storage.get(KEY_PAIR_ID_STORAGE);
        String privateKeyB64 = storage.get(PRIVATE_KEY_STORAGE);
        String publicKeyB64 = storage.get(PUBLIC_KEY_STORAGE);
        String sessionKeyB64 = storage.get(SESSION_KEY_STORAGE);
        String ivB64 = storage.get(IV_STORAGE);

        KeyPair keyPair =
                new KeyPair(
                        RSA.getPubKeyFromBytes(Base64.decodeBase64(publicKeyB64)),
                        RSA.getPrivateKeyFromBytes(Base64.decodeBase64(privateKeyB64)));

        return Optional.of(
                new BankdataCryptoHelperState(
                        keyPairId,
                        keyPair,
                        Base64.decodeBase64(sessionKeyB64),
                        Base64.decodeBase64(ivB64)));
    }

    private static boolean canLoad(final Storage storage) {
        return storage.containsKey(KEY_PAIR_ID_STORAGE)
                && storage.containsKey(PRIVATE_KEY_STORAGE)
                && storage.containsKey(PUBLIC_KEY_STORAGE)
                && storage.containsKey(SESSION_KEY_STORAGE)
                && storage.containsKey(IV_STORAGE);
    }
}
