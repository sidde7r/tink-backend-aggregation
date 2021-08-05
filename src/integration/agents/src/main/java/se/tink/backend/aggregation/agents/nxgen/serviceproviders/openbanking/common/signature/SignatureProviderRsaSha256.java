package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class SignatureProviderRsaSha256 implements QSealSignatureProvider {

    private static final String COLON_SPACE = ": ";
    private static final String NEW_LINE = "\n";
    private static final String SIGNATURE_STRING_FORMAT =
            "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    private static final String RSA_SHA256 = "rsa-sha256";

    private final QsealcSigner qsealcSigner;

    @Inject
    public SignatureProviderRsaSha256(QsealcSigner qsealcSigner) {
        this.qsealcSigner = qsealcSigner;
    }

    @Override
    public String provideSignature(QSealSignatureProviderInput input) {
        List<String> serializedHeaders = new ArrayList<>();
        List<String> headersIncludedInSignature = new ArrayList<>();
        MultivaluedMap<String, Object> headers = input.getRequest().getHeaders();

        for (String key : input.getSignatureHeaders()) {
            List<Object> objects = headers.get(key);
            int size = CollectionUtils.size(objects);
            if (size == 1) {
                headersIncludedInSignature.add(key);
                serializedHeaders.add(serializedHeader(key, objects.get(0).toString()));
            } else if (size > 1) {
                throw new IllegalArgumentException(
                        "Unable to provide more than one value in signature");
            }
        }

        String headersIncludedInSignatureString =
                StringUtils.join(headersIncludedInSignature, StringUtils.SPACE);
        String serializedHeadersString = StringUtils.join(serializedHeaders, NEW_LINE);
        String signatureBase64Sha = signMessage(serializedHeadersString);

        String certificateSerialNumber = getClientSigningCertificateSerialNumber(input);

        return formSignature(
                signatureBase64Sha, headersIncludedInSignatureString, certificateSerialNumber);
    }

    private String getClientSigningCertificateSerialNumber(QSealSignatureProviderInput input) {
        String qseal = input.getQseal();
        switch (input.getCertificateSerialNumberType()) {
            case HEX:
                return getHexClientSigningCertificateSerialNumber(qseal);
            case DECIMAL:
                return getDecimalClientSigningCertificateSerialNumber(qseal);
            default:
                throw new IllegalArgumentException("Serial number type not provided");
        }
    }

    private String serializedHeader(String name, String value) {
        return name.toLowerCase() + COLON_SPACE + value;
    }

    private String signMessage(String toSignString) {
        return qsealcSigner.getSignatureBase64(toSignString.getBytes());
    }

    private String formSignature(
            String signatureBase64Sha, String headers, String certificateSerialNumber) {
        return String.format(
                SIGNATURE_STRING_FORMAT,
                certificateSerialNumber,
                RSA_SHA256,
                headers,
                signatureBase64Sha);
    }

    @SneakyThrows
    private String getHexClientSigningCertificateSerialNumber(String qseal) {
        return CertificateUtils.getSerialNumber(qseal, 16);
    }

    @SneakyThrows
    private String getDecimalClientSigningCertificateSerialNumber(String qseal) {
        return CertificateUtils.getSerialNumber(qseal, 10);
    }
}
