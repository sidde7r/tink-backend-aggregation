package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.lang.reflect.Field;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedKeyManager;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import sun.security.ssl.SSLContextImpl;

public class VolksbankHttpClient {

    private String certificateName;
    private TinkHttpClient tinkClient;

    public VolksbankHttpClient(TinkHttpClient client, String certificateName) {

        this.certificateName = certificateName;
        this.tinkClient = client;
    }

    public void setSslClientCertificate(byte[] certificate, String certificatePassword) {

        tinkClient.setSslClientCertificate(certificate, certificatePassword);

        // To trigger constructInternalClient method
        tinkClient.getInternalClient();
        SSLContext sslContext = tinkClient.getSslContext();

        try {
            ignoreServerTrustList(sslContext);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public TinkHttpClient getTinkClient() {
        return tinkClient;
    }

    private void ignoreServerTrustList(SSLContext sslContext)
            throws NoSuchFieldException, IllegalAccessException {

        Field sslContextSpiField = sslContext.getClass().getDeclaredField("contextSpi");
        sslContextSpiField.setAccessible(true);
        SSLContextImpl spi = (SSLContextImpl) sslContextSpiField.get(sslContext);
        Field keyManagerField = SSLContextImpl.class.getDeclaredField("keyManager");
        keyManagerField.setAccessible(true);
        X509ExtendedKeyManager keyManager = (X509ExtendedKeyManager) keyManagerField.get(spi);
        X509ExtendedKeyManager keyManagerDelegate =
                new X509ExtendedKeyManager() {
                    @Override
                    public String[] getClientAliases(String s, Principal[] principals) {
                        return keyManager.getClientAliases(s, principals);
                    }

                    @Override
                    public String chooseClientAlias(
                            String[] strings, Principal[] principals, Socket socket) {
                        return certificateName;
                    }

                    @Override
                    public String[] getServerAliases(String s, Principal[] principals) {
                        return keyManager.getServerAliases(s, principals);
                    }

                    @Override
                    public String chooseServerAlias(
                            String s, Principal[] principals, Socket socket) {
                        return keyManager.chooseServerAlias(s, principals, socket);
                    }

                    @Override
                    public X509Certificate[] getCertificateChain(String s) {
                        return keyManager.getCertificateChain(s);
                    }

                    @Override
                    public PrivateKey getPrivateKey(String s) {
                        return keyManager.getPrivateKey(s);
                    }
                };

        keyManagerField.set(spi, keyManagerDelegate);
    }
}
