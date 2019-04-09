package se.tink.libraries.repository.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class DatabaseConfiguration {
    @JsonProperty private boolean showSql;
    @NotEmpty @JsonProperty private String driverClass;
    @JsonProperty private String password;
    @JsonProperty private Integer maxPoolSize = 300;
    @NotEmpty @JsonProperty private String url;
    @NotEmpty @JsonProperty private String persistenceUnitName;
    @JsonProperty private boolean enabled = true;
    @JsonProperty private int acquireRetryAttemptsSeconds = -1;
    @JsonProperty private int acquireRetryDelaySeconds = -1;
    @JsonProperty private boolean testConnectionOnCheckout = false;

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @NotEmpty @JsonProperty private String username;

    @JsonProperty private boolean generateDdl = false;

    public String getDriverClass() {
        return driverClass;
    }

    public String getPassword() {
        return password;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public boolean isShowSql() {
        return showSql;
    }

    public Integer getMaxPoolSize() {
        return maxPoolSize;
    }

    public boolean generateDdl() {
        return generateDdl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getAcquireRetryAttemptsSeconds() {
        return acquireRetryAttemptsSeconds;
    }

    public int getAcquireRetryDelaySeconds() {
        return acquireRetryDelaySeconds;
    }

    public boolean getTestConnectionOnCheckout() {
        return testConnectionOnCheckout;
    }
}
