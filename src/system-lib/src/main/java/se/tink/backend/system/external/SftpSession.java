package se.tink.backend.system.external;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import se.tink.backend.utils.LogUtils;

public class SftpSession {

    private final static String KNOWN_HOSTS_FILE = "data/ssh/known_hosts";
    private final static LogUtils log = new LogUtils(SftpSession.class);
    private final Session session;

    public static class Builder {

        private String host;
        private String user;
        private String privateKey;
        private String privateKeyPassphrase;
        private String knownHostsFile;
        private int port = 22;

        public Builder(String user, String host) {
            this.user = user;
            this.host = host;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setPrivateKey(String folderName) {
            this.privateKey = folderName;
            return this;
        }

        public Builder setPrivateKeyPassphrase(String passphrase) {
            this.privateKeyPassphrase = passphrase;
            return this;
        }

        public Builder setKnownHostsFile(String knownHostsFile) {
            this.knownHostsFile = knownHostsFile;
            return this;
        }

        public SftpSession build() {
            return new SftpSession(user, host, port, privateKey, privateKeyPassphrase, knownHostsFile);
        }
    }

    private SftpSession(String user, String host, int port,
            String privateKey,
            String privateKeyPassphrase,
            String knownHostsFile) {

        JSch jsch = new JSch();

        Session session = null;
        try {
            if (knownHostsFile != null) {
                jsch.setKnownHosts(knownHostsFile);
            } else {
                jsch.setKnownHosts(KNOWN_HOSTS_FILE);
            }

            if (privateKey != null) {
                if (privateKeyPassphrase != null) {
                    jsch.addIdentity(privateKey, privateKeyPassphrase);
                } else {
                    jsch.addIdentity(privateKey);
                }
            }

            session = jsch.getSession(user, host, port);
            session.connect();
        } catch (JSchException e) {
            log.error("Could not initialize SftpClient", e);
        }
        this.session = session;
    }

    public boolean isOpen() {
        return session != null && session.isConnected();
    }

    public boolean sendFile(File file, String destination) {
        if (!isOpen()) {
            return false;
        }

        ChannelSftp sftpChannel = null;
        Channel channel = null;
        try {
            channel = session.openChannel("sftp");
            channel.connect();

            sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(file.getAbsolutePath(), destination);

        } catch (JSchException e) {
            log.error("Was not able to send file.", e);
            return false;
        } catch (SftpException e) {
            log.error("Was not able to send file.", e);
            return false;
        } finally {
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
        return true;
    }

    public void close() {
        if (isOpen()) {
            session.disconnect();
        }
    }
}
