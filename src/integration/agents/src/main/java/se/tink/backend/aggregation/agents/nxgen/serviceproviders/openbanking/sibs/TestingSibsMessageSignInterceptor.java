package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SignatureValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.agents.utils.jersey.MessageSignInterceptor;

public class TestingSibsMessageSignInterceptor extends MessageSignInterceptor {

    private static final String PK_PATH = "data/secret/sandbox/pt/sibs/signing.key";
    private static final String CERT_FILE = "data/secret/sandbox/pt/sibs/signing.cer";
    private static final String CERT_NO = "4137f3bde8a30f8c4644e6921246b18c";

    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    public static final List<String> SIGNATURE_HEADERS =
            ImmutableList.of(
                    HeaderKeys.DIGEST,
                    HeaderKeys.TPP_TRANSACTION_ID,
                    HeaderKeys.TPP_REQUEST_ID,
                    HeaderKeys.DATE);

    private PrivateKey privateKey;
    private String certString;
    private SibsConfiguration configuration;

    public TestingSibsMessageSignInterceptor(SibsConfiguration configuration) {
        this.configuration = configuration;
        privateKey = readSigningKey(PK_PATH);
        certString = new String(readFile(CERT_FILE));
    }

    @Override
    protected void appendAdditionalHeaders(ClientRequest request) {
        if (request.getHeaders().get(HeaderKeys.DATE) == null) {
            String requestTimestamp =
                    new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT, Locale.ENGLISH)
                            .format(new Date());
            request.getHeaders().add(HeaderKeys.DATE, requestTimestamp);
        }
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId());
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_CERTIFICATE, certString);
        request.getHeaders()
                .add(SibsConstants.HeaderKeys.TPP_TRANSACTION_ID, SibsUtils.getRequestId());
        request.getHeaders().add(SibsConstants.HeaderKeys.TPP_REQUEST_ID, SibsUtils.getRequestId());
    }

    @Override
    protected void getSignatureAndAddAsHeader(ClientRequest request) {
        List<String> serializedHeaders = new ArrayList<>();
        List<String> listetToSignatureHeaders = new ArrayList<>();

        for (String key : SIGNATURE_HEADERS) {
            if (request.getHeaders().get(key) != null) {
                listetToSignatureHeaders.add(key);
                serializedHeaders.add(serializedHeader(key, request.getHeaders().get(key)));
            }
        }

        String listetToSignatureHeadersString = StringUtils.join(listetToSignatureHeaders, " ");
        String serializedHeadersString = StringUtils.join(serializedHeaders, NEW_LINE);

        String signatureBase64Sha = getSignature(serializedHeadersString);

        String signature = formSignature(signatureBase64Sha, listetToSignatureHeadersString);
        request.getHeaders().add(HeaderKeys.SIGNATURE, signature);
    }

    private String getSignature(String serializedHeadersString) {
        byte[] signatureSha;
        try {
            signatureSha = toSHA256withRSA(serializedHeadersString);
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        String signatureBase64Sha =
                org.apache.commons.codec.binary.Base64.encodeBase64String(signatureSha);
        return signatureBase64Sha;
    }

    private byte[] toSHA256withRSA(String input)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);

        signer.update(input.getBytes());
        return signer.sign();
    }

    @Override
    protected void prepareDigestAndAddAsHeader(ClientRequest request) {
        if (request.getEntity() != null) {
            String digest = SibsUtils.getDigest(request.getEntity());
            request.getHeaders()
                    .add(
                            SibsConstants.HeaderKeys.DIGEST,
                            SibsConstants.HeaderValues.DIGEST_PREFIX + digest);
        }
    }

    private String serializedHeader(String name, List<Object> value) {
        if (value.size() != 1) {
            throw new IllegalArgumentException(
                    "Header serializer supports only single values yet "
                            + name
                            + " is a list "
                            + value.toString());
        }
        return name.toLowerCase() + COLON_SPACE + value.get(0);
    }

    private String formSignature(String signatureBase64Sha, String headers) {
        return String.format(
                Formats.SIGNATURE_STRING_FORMAT,
                CERT_NO,
                SignatureValues.RSA_SHA256,
                headers,
                signatureBase64Sha);
    }

    private PrivateKey readSigningKey(String path) {
        try {
            return KeyFactory.getInstance(Formats.RSA)
                    .generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    java.util.Base64.getDecoder()
                                            .decode(new String(readFile(path)))));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
