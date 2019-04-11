package se.tink.backend.aggregation.storage.database.models;

import java.util.Base64;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "cluster_configurations")
public class ClusterConfiguration {
    @Id private String clusterId;

    @Type(type = "text")
    private String host;

    @Type(type = "text")
    private String apiToken;

    @Type(type = "text")
    private String base64encodedclientcert;

    @Type(type = "boolean")
    private boolean disablerequestcompression;

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getBase64encodedclientcert() {
        return base64encodedclientcert;
    }

    public void setBase64encodedclientcert(String base64encodedclientcert) {
        this.base64encodedclientcert = base64encodedclientcert;
    }

    public boolean isDisablerequestcompression() {
        return disablerequestcompression;
    }

    public void setDisablerequestcompression(boolean disablerequestcompression) {
        this.disablerequestcompression = disablerequestcompression;
    }

    public byte[] getClientCert() {
        return BASE64_DECODER.decode(base64encodedclientcert);
    }
}
