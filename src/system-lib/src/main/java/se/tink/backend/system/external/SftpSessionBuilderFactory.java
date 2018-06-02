package se.tink.backend.system.external;

public class SftpSessionBuilderFactory {

    public SftpSessionBuilderFactory() {

    }

    public SftpSession.Builder createBuilder(String user, String host) {
        return new SftpSession.Builder(user, host);
    }
}
