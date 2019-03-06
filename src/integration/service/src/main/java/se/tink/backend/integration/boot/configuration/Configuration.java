package se.tink.backend.integration.boot.configuration;

public class Configuration {
    private String grpcTlsCertificatePath = "/tls/tls.crt";
    private String grpcTlsKeyPath = "/tls/tls.key";

    public String getGrpcTlsCertificatePath() {
        return grpcTlsCertificatePath;
    }

    public void setGrpcTlsCertificatePath(String grpcTlsCertificatePath) {
        this.grpcTlsCertificatePath = grpcTlsCertificatePath;
    }

    public String getGrpcTlsKeyPath() {
        return grpcTlsKeyPath;
    }

    public void setGrpcTlsKeyPath(String grpcTlsKeyPath) {
        this.grpcTlsKeyPath = grpcTlsKeyPath;
    }
}
