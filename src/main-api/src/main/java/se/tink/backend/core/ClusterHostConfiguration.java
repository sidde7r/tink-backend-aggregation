package se.tink.backend.core;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "cluster_host_configuration")
public class ClusterHostConfiguration {
    private static final Base64 BASE64 = new Base64();

    @Id
    private String clusterId;
    @Type(type = "text")
    private String host;
    @Type(type = "text")
    private String apiToken;
    @Type(type = "text")
    private String base64EncodedClientCertificate;
    private boolean disableRequestCompression;
    private String aggregatorIdentifier;


    public String getClusterId() {
        return clusterId;
    }

    public String getHost() {
        return host;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getBase64EncodedClientCertificate() {
        return base64EncodedClientCertificate;
    }

    public byte[] getClientCertificate() {
        return BASE64.decode(base64EncodedClientCertificate);
    }

    public boolean isDisableRequestCompression() {
        return disableRequestCompression;
    }

    public String getAggregatorIdentifier(){
        return aggregatorIdentifier;
    }

    public boolean isValid() {
        if (Objects.isNull(clusterId)) {
            return false;
        }

        if (Objects.isNull(host)) {
            return false;
        }

        if (Objects.isNull(apiToken)) {
            return false;
        }

        if (Objects.isNull(base64EncodedClientCertificate)) {
            return false;
        }

        if (Objects.isNull(aggregatorIdentifier)) {
            return false;
        }

        return true;
    }
}
