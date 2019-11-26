package se.tink.sa.framework.tools;

public interface SecretsHandler {

    String getClientId();

    String getClientSecret();

    String getCertificate();

    String getCertificateSerialNumber();
}
