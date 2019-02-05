package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLUtils;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities.CFRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities.PayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.entities.QueryParameterEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.rpc.QueryResponse;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class WLFetcher {
    private static final Logger logger = LoggerFactory.getLogger(WLFetcher.class);
    //private static final AggregationLogger longLogger = new AggregationLogger(WLFetcher.class);

    private final WLApiClient apiClient;
    private final WLFetcherStorage storage;
    private final WLConfig config;

    public WLFetcher(
            final WLApiClient client,
            final WLFetcherStorage storage,
            final WLConfig config) {
        this.apiClient = client;
        this.storage = storage;
        this.config = config;
    }

    public <T> T getAccounts(final Class<T> entityClass) {
        return query(WLConstants.Procedure.fetch_FinancialStatusAccountDO.name(), entityClass, new JSONObject());
    }

    public <T> T getAccountHolders(final Class<T> entityClass) {
        return query(WLConstants.Procedure.fetch_AdvisorsListDO.name(), entityClass, new JSONObject());
    }

    public <T> T getTransactions(final Class<T> entityClass, final String accountNumber) {
        final JSONObject paylIWV;
        try {
            paylIWV = new JSONObject().put("key1", accountNumber);
        } catch (JSONException e) {
            throw new IllegalStateException();
        }
        return query(WLConstants.Procedure.fetch_AccountDO.name(), entityClass, paylIWV);
    }

    private String getApiPath(final String slug) {
        return WLConstants.Url.API_ROOT + config.getModuleName() + slug;
    }

    private <T> T query(final String procedure, final Class<T> entityClass, final JSONObject payload) {
        final Form request = new Form.Builder()
                .put(WLConstants.Forms.ADAPTER, WLConstants.Forms.ADAPTER_FACADE)
                .put(WLConstants.Forms.PROCEDURE, WLConstants.Forms.FETCH_CRYPT)
                .put(WLConstants.Forms.COMPRESS_RESPONSE)
                .put(WLConstants.Forms.PARAMETERS,
                        SerializationUtils.serializeToString(getParameters(procedure, payload)))
                .build();
        final HttpResponse httpResponse = apiClient.getClient()
                .request(config.getEndpointUrl() + getApiPath(WLConstants.Url.QUERY))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header(WLConstants.Headers.X_WL_APP_VERSION, WLConstants.WL_APP_VERSION)
                .header(WLConstants.Storage.WL_INSTANCE_ID, storage.getWlInstanceId())
                .body(request.serialize())
                .post(HttpResponse.class);

        final QueryResponse responseBody = WLUtils.encasedJsonToEntity(httpResponse, QueryResponse.class);

        Preconditions.checkArgument(responseBody.getIsSuccessful());

        final byte[] ciphertextBytes = EncodingUtils.decodeBase64String(responseBody.getCipherText());
        final byte[] signatureBytes = EncodingUtils.decodeBase64String(responseBody.getSignature());

        final boolean isSignatureValid = verifySignature(config.getPublicKey(), signatureBytes, ciphertextBytes);
        if (!isSignatureValid) {
            logger.error("Signature for symmetric encryption was found to be invalid");
            throw new IllegalStateException();
        }

        final byte[] sharedAesKey = EncodingUtils.decodeBase64String(storage.getSharedAesKey());
        final byte[] sharedAesIv = EncodingUtils.decodeBase64String(storage.getSharedAesIv());
        final byte[] plaintext = AES.decryptCbc(sharedAesKey, sharedAesIv, ciphertextBytes);

        final String plaintextString = new String(plaintext);
        // TODO consider moving to caller
        //longLogger.infoExtraLong(plaintextString, WLConstants.LogTags.WL_RECEIVED_PLAINTEXT.toTag());
        return SerializationUtils.deserializeFromString(plaintextString, entityClass);
    }

    private static byte[] randomBytes(final int byteCount) {
        final byte[] bytes = new byte[byteCount];
        try {
            SecureRandom.getInstance("SHA1PRNG").nextBytes(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        return bytes;
    }

    private byte[] rsaEncrypt(final byte[] plaintext) {
        return RSA.encryptEcbPkcs1(config.getPublicKey(), plaintext);
    }

    public static String generateHexstring() {
        return DatatypeConverter.printHexBinary(randomBytes(32));
    }

    public static byte[] generateAesKey() {
        return randomBytes(16);
    }

    public static byte[] generateAesIv() {
        return randomBytes(16);
    }

    private List<QueryParameterEntity> getParameters(final String procedure, final JSONObject paylIWV) {
        // Will later be found in the response dict within the 'z' field
        final String hash = generateHexstring();
        // Needed by the server to decrypt the message
        final byte[] sharedAesKey = generateAesKey();
        storage.setSharedAesKey(EncodingUtils.encodeAsBase64String(sharedAesKey));
        // Needed by the server to decrypt the message
        final byte[] sharedAesIv = generateAesIv();
        storage.setSharedAesIv(EncodingUtils.encodeAsBase64String(sharedAesIv));
        // Client-side thingie; probably not important
        final byte[] z = randomBytes(32);
        // Actual message
        final String plaintext = SerializationUtils.serializeToString(
                new CFRequest(paylIWV.toString(), DatatypeConverter.printHexBinary(z), "0")
        );

        // TODO consider moving to caller
        //longLogger.infoExtraLong(plaintext, WLConstants.LogTags.WL_SENT_PLAINTEXT.toTag());

        // Actual message, encrypted and base64 encoded
        final byte[] ciphertext = AES.encryptCbc(sharedAesKey, sharedAesIv, plaintext.getBytes());

        final PayloadEntity payload = new PayloadEntity();
        payload.setH(hash);
        payload.setA(EncodingUtils.encodeAsBase64String(rsaEncrypt(sharedAesKey)));
        payload.setB(EncodingUtils.encodeAsBase64String(rsaEncrypt(sharedAesIv)));
        payload.setC(EncodingUtils.encodeAsBase64String(ciphertext));

        QueryParameterEntity parameter = new QueryParameterEntity();
        parameter.setPayloadEntity(payload);
        parameter.setProcedure(procedure);

        return Collections.singletonList(parameter);
    }

    private static boolean verifySignature(final PublicKey publicKey, byte[] signatureBytes, final byte[] ciphertext) {
        final Signature signature;
        try {
            signature = Signature.getInstance("SHA1withRSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        try {
            signature.initVerify(publicKey);
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        try {
            signature.update(ciphertext);
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
        final boolean isSignatureValid;
        try {
            isSignatureValid = signature.verify(signatureBytes);
        } catch (SignatureException e) {
            throw new IllegalStateException(e);
        }
        return isSignatureValid;
    }
}
