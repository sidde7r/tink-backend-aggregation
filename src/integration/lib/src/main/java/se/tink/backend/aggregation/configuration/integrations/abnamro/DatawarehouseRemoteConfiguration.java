package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatawarehouseRemoteConfiguration {
    /* Setting this to true is against Tink cryptography policy */
    @JsonProperty private boolean useDiffieHellmanSha1 = false; // TODO Remove me
    @JsonProperty private String host = "localhost";
    @JsonProperty private String remotePath = "/tmp/";
    @JsonProperty private int remotePort = 22;
    @JsonProperty private boolean deleteFileWhenDone = true;
    @JsonProperty private boolean useSftp = false;

    public boolean shouldUseDiffieHellmanSha1() {
        return useDiffieHellmanSha1;
    }

    public String getHost() {
        return host;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public boolean shouldDeleteFileWhenDone() {
        return deleteFileWhenDone;
    }

    public boolean shouldUseSftp() {
        return useSftp;
    }
}
