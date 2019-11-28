package se.tink.sa.framework.security;

import java.security.KeyStore;

public interface EidasServiceVerificationCertsLoader {

    KeyStore getRootCaTrustStore();

    KeyStore getClientCertKeystore();
}
