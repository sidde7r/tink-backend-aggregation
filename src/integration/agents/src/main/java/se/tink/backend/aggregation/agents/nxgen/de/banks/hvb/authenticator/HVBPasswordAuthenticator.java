package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.WLPasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public final class HVBPasswordAuthenticator implements PasswordAuthenticator {
    private final WLPasswordAuthenticator wlAuthenticator;
    private final HVBStorage storage;

    public HVBPasswordAuthenticator(final WLApiClient client, final HVBStorage storage, final WLConfig wlConfig) {
        this.wlAuthenticator = new WLPasswordAuthenticator(client, storage, wlConfig);
        this.storage = storage;
    }

    public static RSAPublicKey certificateStringToPublicKey(final String certificate) {
        final String certificatePem = "-----BEGIN CERTIFICATE-----\n" + certificate + "\n-----END CERTIFICATE-----";
        try {
            return (RSAPublicKey) CertificateFactory.getInstance(HVBConstants.CERT_TYPE)
                    .generateCertificate(new ByteArrayInputStream(certificatePem.getBytes()))
                    .getPublicKey();
        } catch (CertificateException e) {
            throw new SecurityException(
                    String.format("Invalid %s certificate", HVBConstants.CERT_TYPE), e);
        }
    }

    /**
     * Will add WL-Instance-Id to storage.
     */
    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException {
        wlAuthenticator.authenticate(username, password);

        try {
            storage.getWlInstanceId();
        } catch (NoSuchElementException e) { // Indicates a bug in the authenticator
            throw new IllegalStateException("WL-Instance-Id was not set during authentication");
        }
    }
}
