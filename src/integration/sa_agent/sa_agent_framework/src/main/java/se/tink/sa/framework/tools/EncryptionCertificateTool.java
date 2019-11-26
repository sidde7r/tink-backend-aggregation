package se.tink.sa.framework.tools;

public interface EncryptionCertificateTool {

    String getCertificate();

    byte[] toSHA256withRSA(String content);

    String getCertificateSerialNumber();
}
