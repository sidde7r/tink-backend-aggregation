package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@FilterOrder(category = FilterPhases.SEND, order = 0)
public class ProxyFilter extends Filter {

    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int SIGNATURE_LENGTH = 64;
    private static final int IV_LENGTH = 16;

    private final IngStorage ingStorage;

    private final IngCryptoUtils ingCryptoUtils;

    private final IngLoggingAdapter ingLoggingAdapter;

    private final ObjectMapper mapper = new ObjectMapper();

    private final boolean debug;

    public ProxyFilter(
            IngStorage ingStorage,
            IngCryptoUtils ingCryptoUtils,
            IngLoggingAdapter ingLoggingAdapter) {
        this.ingStorage = ingStorage;
        this.ingCryptoUtils = ingCryptoUtils;
        this.ingLoggingAdapter = ingLoggingAdapter;
        this.debug = false; // DO NOT USE ON PROD. ONLY FOR LOCAL DEBUGGING
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        handleRequest(httpRequest);
        HttpResponse response = nextFilter(httpRequest);
        handleResponse(response);
        return response;
    }

    private void handleRequest(HttpRequest httpRequest) {
        serialize(httpRequest);
        logRequest(httpRequest);
        encrypt(httpRequest);
        sign(httpRequest);
        if (debug) {
            BodyPrinter.print(httpRequest);
        }
    }

    private void handleResponse(HttpResponse httpResponse) {
        if (debug) {
            BodyPrinter.print(httpResponse);
        }
        validateResponse(httpResponse);
        verifySign(httpResponse);
        decrypt(httpResponse);
        logResponse(httpResponse);
        changeMimeType(httpResponse);
    }

    private void serialize(HttpRequest request) {
        try {
            request.setBody(escapedSlashes(mapper.writeValueAsString(request.getBody())));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize body", e);
        }
    }

    private String escapedSlashes(String body) {
        return body.replace("/", "\\/");
    }

    private void encrypt(HttpRequest request) {
        String body = (String) request.getBody();
        byte[] iv = ingCryptoUtils.getRandomBytes(IV_LENGTH);
        byte[] bytes = AES.encryptCbcPkcs7(getEncryptionKey().getEncoded(), iv, body.getBytes());
        replaceBody(request, bytes, iv);
    }

    private void sign(HttpRequest request) {
        byte[] body = (byte[]) request.getBody();
        byte[] signature = ingCryptoUtils.calculateSignature(body, getSigningKey());
        replaceBody(request, body, signature);
    }

    private void validateResponse(HttpResponse httpResponse) {
        byte[] body = httpResponse.getBody(byte[].class);
        boolean isBodyValid = validateBody(body);
        if (!isBodyValid) {
            resetStreamForLogging(httpResponse);
            logResponse(httpResponse);
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    private void verifySign(HttpResponse httpResponse) {
        byte[] body = httpResponse.getBody(byte[].class);
        byte[] messageWithIV = parseMessageWithIV(body);
        byte[] signature = parseSignature(body);
        byte[] expected = ingCryptoUtils.calculateSignature(messageWithIV, getSigningKey());

        if (!Arrays.equals(expected, signature) && logger.isWarnEnabled()) {
            logger.warn(
                    "Incorrect signature received {} <> {}",
                    Hex.toHexString(expected),
                    Hex.toHexString(signature));
        }
    }

    private void decrypt(HttpResponse httpResponse) {
        byte[] body = httpResponse.getBody(byte[].class);

        byte[] encrypted = parseMessage(body);
        byte[] iv = parseIV(body);
        byte[] decrypted = AES.decryptCbcPkcs7(getEncryptionKey().getEncoded(), iv, encrypted);

        httpResponse
                .getInternalResponse()
                .setEntityInputStream(new ByteArrayInputStream(decrypted));
    }

    private void changeMimeType(HttpResponse response) {
        response.getHeaders().remove("Content-Type");
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
    }

    private byte[] parseMessage(byte[] body) {
        return Arrays.copyOfRange(body, 0, body.length - SIGNATURE_LENGTH - IV_LENGTH);
    }

    private boolean validateBody(byte[] body) {
        // Messages that are returned and handled by this filter must be encrypted and signed.
        // If the body is shorter than the signature then it is most probably a temporary server
        // side failure. Like unencrypted and unsigned: `{"status":"502"}`
        return body.length >= SIGNATURE_LENGTH;
    }

    private byte[] parseMessageWithIV(byte[] body) {
        return Arrays.copyOfRange(body, 0, body.length - SIGNATURE_LENGTH);
    }

    private byte[] parseIV(byte[] body) {
        return Arrays.copyOfRange(
                body, body.length - SIGNATURE_LENGTH - IV_LENGTH, body.length - SIGNATURE_LENGTH);
    }

    private byte[] parseSignature(byte[] body) {
        return Arrays.copyOfRange(body, body.length - SIGNATURE_LENGTH, body.length);
    }

    private void replaceBody(HttpRequest request, byte[]... bodyParts) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (byte[] bodyPart : bodyParts) {
                byteArrayOutputStream.write(bodyPart);
            }
            request.setBody(byteArrayOutputStream.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("Could not replace request body", ex);
        }
    }

    private SecretKeySpec getSigningKey() {
        return ingStorage.getSigningKey();
    }

    private SecretKeySpec getEncryptionKey() {
        return ingStorage.getEncryptionKey();
    }

    private void logRequest(HttpRequest httpRequest) {
        ingLoggingAdapter.logRequest(httpRequest);
    }

    private void logResponse(HttpResponse httpResponse) {
        ingLoggingAdapter.logResponse(httpResponse);
    }

    private void resetStreamForLogging(HttpResponse httpResponse) {
        try {
            httpResponse.getBodyInputStream().reset();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not reset input stream");
        }
    }
}
