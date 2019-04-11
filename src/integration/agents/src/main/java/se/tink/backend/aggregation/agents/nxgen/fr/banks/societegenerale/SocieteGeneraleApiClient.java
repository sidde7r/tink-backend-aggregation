package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.AuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.entities.LoginGridData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.LoginGridResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.AccountsData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.TransactionsData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SocieteGeneraleApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(SocieteGeneraleApiClient.class);

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public SocieteGeneraleApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public Optional<AccountsData> getAccounts() {

        String raw =
                client.request(SocieteGeneraleConstants.Url.SBM_MOB_MOB_SBM_RLV_SNT_CPT)
                        .get(String.class);

        return extractData(raw, AccountsResponse.class, AccountsData.class);
    }

    public GenericResponse<?> getAuthInfo() {
        return client.request(SocieteGeneraleConstants.Url.GET_AUTH_INFO)
                .queryParam(
                        SocieteGeneraleConstants.QueryParam.NIV_AUTHENT,
                        SocieteGeneraleConstants.Default.AUTHENTIFIE)
                .get(GenericResponse.Any.class);
    }

    public Optional<LoginGridData> getLoginGrid() {

        String raw =
                client.request(SocieteGeneraleConstants.Url.SEC_VK_GEN_CRYPTO).get(String.class);

        return extractData(raw, LoginGridResponse.class, LoginGridData.class);
    }

    public byte[] getLoginNumPadImage(String crypto) {
        return client.request(SocieteGeneraleConstants.Url.SEC_VK_GEN_UI)
                .queryParam(
                        SocieteGeneraleConstants.QueryParam.MODE_CLAVIER,
                        SocieteGeneraleConstants.Default.ZERO)
                .queryParam(
                        SocieteGeneraleConstants.QueryParam.VK_VISUEL,
                        SocieteGeneraleConstants.Default.VK_WIDESCREEN)
                .queryParam(SocieteGeneraleConstants.QueryParam.CRYPTOGRAMME, crypto)
                .get(byte[].class);
    }

    public GenericResponse<?> getLogout() {
        return client.request(SocieteGeneraleConstants.Url.LOGOUT).get(GenericResponse.Any.class);
    }

    public Optional<TransactionsData> getTransactions(
            String technicalId, String technicalCardId, int page, int pageSize) {

        String raw =
                client.request(SocieteGeneraleConstants.Url.ABM_RESTIT_CAV_LISTE_OPERATIONS)
                        .queryParam(SocieteGeneraleConstants.QueryParam.B_64_ID_PRESTA, technicalId)
                        .queryParam(
                                SocieteGeneraleConstants.QueryParam.B_64_NUMERO_CARTE,
                                technicalCardId)
                        .queryParam(
                                SocieteGeneraleConstants.QueryParam.A_100_TIMESTAMPREF,
                                SocieteGeneraleConstants.Default.EMPTY)
                        .queryParam(
                                SocieteGeneraleConstants.QueryParam.N_15_NB_OCC,
                                Integer.toString(pageSize))
                        .queryParam(
                                SocieteGeneraleConstants.QueryParam.N_15_RANG_OCC,
                                Integer.toString(1 + (page * pageSize)))
                        .get(String.class);

        return extractData(raw, TransactionsResponse.class, TransactionsData.class);
    }

    public Optional<AuthenticationData> postAuthentication(
            String user_id, String cryptocvcs, String codsec) {

        String deviceId = getDeviceId();
        String token = getToken();

        AuthenticationRequest formBody =
                AuthenticationRequest.create(user_id, cryptocvcs, codsec, deviceId, token);

        String raw =
                client.request(SocieteGeneraleConstants.Url.SEC_VK_AUTHENT)
                        .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(String.class);

        return extractData(raw, AuthenticationResponse.class, AuthenticationData.class);
    }

    private <T> Optional<T> extractData(
            String raw, Class<? extends GenericResponse<T>> wrapperClass, Class<T> valueType) {

        T retVal = null;

        try {

            GenericResponse.Any generic = MAPPER.readValue(raw, GenericResponse.Any.class);

            if (generic.isOk()) {

                if (generic.isEncrypted()) {

                    String encryptedDataAsString = generic.getData().toString();
                    String decryptedStringToParse = decrypt(encryptedDataAsString);
                    retVal = MAPPER.readValue(decryptedStringToParse, valueType);

                } else {

                    retVal = MAPPER.readValue(raw, wrapperClass).getData();
                }

            } else {

                logger.error(
                        "{} Request NOK: {}", SocieteGeneraleConstants.Logging.REQUEST_NOT_OK, raw);
            }

        } catch (IOException e) {
            logger.error(
                    "{} Failed to parse (or decrypt): {}",
                    SocieteGeneraleConstants.Logging.PARSE_FAILURE,
                    raw);
            throw new IllegalStateException(e);
        }

        return Optional.ofNullable(retVal);
    }

    private String getDeviceId() {
        return persistentStorage.get(SocieteGeneraleConstants.StorageKey.DEVICE_ID);
    }

    private String getSessionKey() {
        return sessionStorage.get(SocieteGeneraleConstants.StorageKey.SESSION_KEY);
    }

    private String getToken() {
        String token = persistentStorage.get(SocieteGeneraleConstants.StorageKey.TOKEN);
        return token != null ? token : "";
    }

    private int getIvLength(byte[] data) {
        int ivLength = 0;
        ivLength |= (data[0] & 0xFF) << 24;
        ivLength |= (data[1] & 0xFF) << 16;
        ivLength |= (data[2] & 0xFF) << 8;
        ivLength |= (data[3] & 0xFF);
        return ivLength;
    }

    private byte[] getIv(byte[] data) {
        int ivLength = getIvLength(data);
        return Arrays.copyOfRange(data, 4, 4 + ivLength);
    }

    private byte[] getCipherText(byte[] data) {
        int ivLength = getIvLength(data);
        return Arrays.copyOfRange(data, 4 + ivLength, data.length);
    }

    private String decrypt(String dataToDecrypt) {

        // key == "clesession" in response from POST /sec/vk/authent.json HTTP/1.1.
        // This key is associated with a keyId, which is the value of "id_cle" in the same response.
        // The app keeps a dictionary of <keyId, key>, they might send us multiple keys at a later
        // point.

        String sessionKey = getSessionKey();
        byte[] key = EncodingUtils.decodeBase64String(sessionKey);

        // data == "donnees" in response from w/e
        byte[] data = EncodingUtils.decodeBase64String(dataToDecrypt);

        byte[] iv = getIv(data);
        byte[] cipherText = getCipherText(data);
        byte[] plainText = AES.decryptCbc(key, iv, cipherText);

        return new String(plainText);
    }
}
