package se.tink.backend.aggregation.configuration.agents.utils;

import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;

/**
 * This is a utility class to extract various data from certificate retrieved from Secrets Service.
 * The expected input is always base64 encoded PEM.
 */
public class CertificateUtils {

    private static final String ERROR_COULD_NOT_FIND_CERT = "Couldn't find a certificate.";

    private CertificateUtils() {
        throw new AssertionError();
    }

    /**
     * From qcStatement extension, extract the business scopes (AIS and/or PIS) that the cert is
     * capable of.
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @return list of business scopes
     * @throws IOException
     */
    public static List<CertificateBusinessScope> getBusinessScopeFromCertificate(
            String base64EncodedCertificates) throws IOException {
        ArrayList<X509CertificateHolder> x509CertificateHolder =
                getCertificateHolderFromBase64EncodedCert(base64EncodedCertificates);
        if (x509CertificateHolder.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        List<CertificateBusinessScope> result = new ArrayList<>(2);
        String extensionContainingScope = getScopeInfoExtension(x509CertificateHolder.get(0));
        if (extensionContainingScope.contains("PSP_AI")) {
            result.add(CertificateBusinessScope.AIS);
        }
        if (extensionContainingScope.contains("PSP_PI")) {
            result.add(CertificateBusinessScope.PIS);
        }
        return result;
    }

    /**
     * Convert the base64 encoded PEM to based64 encoded DER. I.e. "MII ..." without plain-text
     * anchor lines (BEGIN/END CERTIFICATE)
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @return based64 encoded cert, e.g. "MII......".
     * @throws CertificateException
     */
    public static String getDerEncodedCertFromBase64EncodedCertificate(
            String base64EncodedCertificates) throws CertificateException {
        List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        return Base64.getEncoder().encodeToString(certs.get(0).getEncoded());
    }

    /**
     * Extract public key PEM from base64 encoded PEM
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @return Base64 encoded public key
     * @throws CertificateException
     */
    public static String getPublicKeyPem(String base64EncodedCertificates)
            throws CertificateException {
        List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        PublicKey key = certs.get(0).getPublicKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Extract serial number of the certificate
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @param radix set 10 to get decimal format, 16 to get hex format
     * @return serial number of the certificate in expected radix.
     * @throws CertificateException
     */
    public static String getSerialNumber(String base64EncodedCertificates, int radix)
            throws CertificateException {
        List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        return certs.get(0).getSerialNumber().toString(radix);
    }

    public enum CANameEncoding {
        /** Add CA name as plain text */
        PLAINTEXT,
        /** Encode CA name as Base64 */
        BASE64,
        /**
         * Encode CA name as Base64 only if it contains non-ascii characters, otherwise plain text
         */
        BASE64_IF_NOT_ASCII;

        String encode(String value) {
            switch (this) {
                case PLAINTEXT:
                    return value;
                case BASE64:
                    return base64Encode(value);
                case BASE64_IF_NOT_ASCII:
                    return Charsets.US_ASCII.newEncoder().canEncode(value)
                            ? value
                            : base64Encode(value);
            }
            throw new IllegalStateException("Unknown CANameEncoding");
        }

        private String base64Encode(String value) {
            return Base64.getEncoder().encodeToString(value.getBytes(Charsets.UTF_8));
        }
    }

    /**
     * Get the CA name of the certificate, optionally encoded in base64.
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @param caNameEncoding how to encode the CA name
     * @return CA name of the certificate.
     * @throws CertificateException
     */
    public static String getCAName(String base64EncodedCertificates, CANameEncoding caNameEncoding)
            throws CertificateException {
        final List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        return caNameEncoding.encode(
                certs.get(0).getIssuerX500Principal().getName(X500Principal.RFC1779));
    }

    /**
     * Extract organization identifier from base64 encoded PEM
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate
     * @return organization identifier.
     * @throws CertificateException
     */
    public static String getOrganizationIdentifier(String base64EncodedCertificates)
            throws CertificateException {
        List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        return new X500Name(certs.get(0).getSubjectX500Principal().getName())
                .getRDNs(BCStyle.ORGANIZATION_IDENTIFIER)[0]
                .getFirst()
                .getValue()
                .toString();
    }

    /**
     * Extract full Distinguished Name (DN) of a certificate issuer from base64 encoded PEM
     *
     * @param base64EncodedCertificates base64 encoded PEM of an eIDAS certificate/certificates
     * @return certificate issuer distinguished name
     * @throws CertificateException
     */
    public static String getCertificateIssuerDN(String base64EncodedCertificates)
            throws CertificateException {
        List<X509Certificate> certs =
                getX509CertificatesFromBase64EncodedCert(base64EncodedCertificates);
        if (certs.isEmpty()) {
            throw new IllegalStateException(ERROR_COULD_NOT_FIND_CERT);
        }
        return certs.get(0).getIssuerX500Principal().getName();
    }

    private static String getScopeInfoExtension(X509CertificateHolder x509CertificateHolder)
            throws IOException {
        String oid = Extension.qCStatements.getId();
        Extension scopeInfoExtension =
                x509CertificateHolder.getExtension(new ASN1ObjectIdentifier(oid));
        if (scopeInfoExtension != null) {
            return new String(scopeInfoExtension.getExtnValue().getEncoded());
        }
        return "";
    }

    // To use bouncy castle
    private static ArrayList<X509CertificateHolder> getCertificateHolderFromBase64EncodedCert(
            String base64EncodedCertificates) throws IOException {
        ArrayList<X509CertificateHolder> certs = new ArrayList<>();
        PEMParser reader =
                new PEMParser(
                        new StringReader(
                                new String(Base64.getDecoder().decode(base64EncodedCertificates))));
        X509CertificateHolder crt;
        while ((crt = (X509CertificateHolder) reader.readObject()) != null) {
            certs.add(crt);
        }
        if (certs.isEmpty()) {
            throw new IllegalStateException("Couldn't find a certificate in the specified file.");
        }
        return certs;
    }

    public static List<X509Certificate> getX509CertificatesFromBase64EncodedCert(
            String base64EncodedCertificates) throws CertificateException {
        return X509CertificateToBytesStream(base64EncodedCertificates)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static Optional<X509Certificate> getRootX509CertificateFromBase64EncodedString(
            String base64EncodedCertificates) throws CertificateException {
        return X509CertificateToBytesStream(base64EncodedCertificates).findFirst();
    }

    private static Stream<X509Certificate> X509CertificateToBytesStream(
            String base64EncodedCertificates) throws CertificateException {
        return CertificateFactory.getInstance("X.509")
                .generateCertificates(
                        new ByteArrayInputStream(
                                Base64.getDecoder().decode(base64EncodedCertificates)))
                .stream()
                .map(X509Certificate.class::cast);
    }
}
