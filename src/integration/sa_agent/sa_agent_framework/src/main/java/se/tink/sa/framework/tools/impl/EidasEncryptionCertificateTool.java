package se.tink.sa.framework.tools.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.tink.sa.framework.rest.client.EidasProxyClient;
import se.tink.sa.framework.tools.EncryptionCertificateTool;
import se.tink.sa.framework.tools.QsealcAlg;
import se.tink.sa.framework.tools.SecretsHandler;

@Component
public class EidasEncryptionCertificateTool implements EncryptionCertificateTool {

    @Value("${security.eidas.proxy.service.algorithm}")
    private QsealcAlg qsealAlg;

    @Value("${security.eidas.proxy.service.appId}")
    private String appId;

    @Value("${security.eidas.proxy.service.clusterId}")
    private String clusterId;

    @Autowired private EidasProxyClient eidasProxyClient;

    @Autowired private SecretsHandler secretsHandler;

    @Override
    public String getCertificate() {
        return secretsHandler.getCertificate();
    }

    @Override
    public byte[] toSHA256withRSA(String content) {
        String requester = this.getClass().getCanonicalName();
        return eidasProxyClient.callSecretsService(
                content.getBytes(), appId, clusterId, qsealAlg, requester);
    }

    @Override
    public String getCertificateSerialNumber() {
        return secretsHandler.getCertificateSerialNumber();
    }
}
