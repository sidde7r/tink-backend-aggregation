package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.hibernate.validator.constraints.NotEmpty;

public class DistributedDatabaseConfiguration {

    private static final String DEFAULT_KEYSPACE = "tink";

    /**
     * Whether we should issue "CREATE KEYSPACE tink" on start. This can be disabled in production to narrow down what
     * the Tink application's users are allowed to do.
     */
    @JsonProperty
    private boolean createKeyspaceOnStart = true;
    
    private static final String DEFAULT_DATACENTER = "Cassandra";
    private static final int DEFAULT_REMOTE_FALLBACK_SERVERS = 2;

    /**
     * The default batch size that we use when saving or deleting transactions. Can be higher or lower depending on
     * the amount of information that a transaction contains.
     */
    private static final int DEFAULT_BATCH_SIZE = 30;

    @NotEmpty
    private boolean enabled = false;
    @JsonProperty
    private String keyspace = DEFAULT_KEYSPACE;
    @JsonProperty
    private String password;
    @NotEmpty
    @JsonProperty
    private List<String> seeds;
    @JsonProperty
    private String username;
    @JsonProperty
    private String datacenter = DEFAULT_DATACENTER;
    @JsonProperty
    private int port = 9042;
    @JsonProperty
    private int remoteFallbackServers = DEFAULT_REMOTE_FALLBACK_SERVERS;
    @JsonProperty
    private int batchSize = DEFAULT_BATCH_SIZE;

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public List<String> getSeeds() {
        return seeds;
    }

    public String getUsername() {
        return username;
    }

    boolean getCreateKeyspaceOnStart() {
        return createKeyspaceOnStart;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSeeds(List<String> seeds) {
        this.seeds = seeds;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getDatacenter() {
        return datacenter;
    }
    
    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    int getRemoteFallbackServers() {
        return remoteFallbackServers;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
