package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.Crypto.IV_SIZE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.primitives.Bytes;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RequiredArgsConstructor
public class BankdataCryptoHelper implements Decryptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private BankdataCryptoHelperState state;

    public void loadState(BankdataCryptoHelperState state) {
        this.state = state;
    }

    public boolean isStateInitialized() {
        return state != null;
    }

    // TODO: generalize a bit.
    public String enrollCrypt() {

        String publicKey =
                EncodingUtils.encodeAsBase64String(state.getKeyPair().getPublic().getEncoded());
        String jdata = buildJSONData(state.getKeyPairId(), publicKey);

        byte[] actualAESInputDataInBytes = jdata.getBytes();
        byte[] encryptedData =
                AES.encryptCbcPkcs7(
                        state.getSessionKey(), state.getIv(), actualAESInputDataInBytes);
        byte[] wholePackage = Bytes.concat(state.getIv(), encryptedData);
        return EncodingUtils.encodeAsBase64String(wholePackage);
    }

    /**
     * Encrypt our generated session key with their public key. They will then use our session key
     * for symmetric encryption.
     */
    public String getEncryptedSessionKey() {
        RSAPublicKey bankdataPublicKey =
                RSA.getPubKeyFromBytes(Base64.decodeBase64(BankdataConstants.Crypto.CERTIFICATE));

        return new String(
                Base64.encodeBase64(
                        RSA.encryptNoneOaepMgf1(bankdataPublicKey, state.getSessionKey())),
                StandardCharsets.UTF_8);
    }

    /**
     * Encrypt data with sessionKey and iv, and prepends the iv.
     *
     * @param data data to encrypt
     * @return String derived from: b64(iv + encrypt(data))
     */
    public String encrypt(byte[] data) {
        return new String(
                Base64.encodeBase64(
                        Bytes.concat(
                                state.getIv(),
                                AES.encryptCbc(state.getSessionKey(), state.getIv(), data))),
                StandardCharsets.UTF_8);
    }

    /**
     * Decrypts given data using sessionKey and iv. Removes prepended iv.
     *
     * @param data String in b64 format
     * @return byte[] of encrypted data, with prepended iv removed.
     */
    @Override
    public byte[] decrypt(String data) {
        byte[] decryptedBytes =
                AES.decryptCbc(state.getSessionKey(), state.getIv(), Base64.decodeBase64(data));
        return Arrays.copyOfRange(decryptedBytes, IV_SIZE, decryptedBytes.length);
    }

    private static String buildJSONData(String keyId, String publicKey) {
        ObjectNode object = objectMapper.createObjectNode();
        object.put("keyId", keyId);
        object.put("publicKey", publicKey);
        return object.toString();
    }
}
