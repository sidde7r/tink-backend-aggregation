package se.tink.backend.integration.tpp_secrets_service.client.entities;

import java.util.List;
import java.util.Map;

public class SecretsEntityCore {

    private Map<String, String> secrets;
    private List<String> redirectUrls;
    private List<String> scopes;
    private String qwac;
    private String qsealc;

    public String getQwac() {
        return qwac;
    }

    public String getQsealc() {
        return qsealc;
    }

    public Map<String, String> getSecrets() {
        return secrets;
    }

    public List<String> getRedirectUrls() {
        return redirectUrls;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public static final class Builder {

        private Map<String, String> secrets;
        private List<String> redirectUrls;
        private List<String> scopes;
        private String qwac;
        private String qsealc;

        public Builder() {}

        public Builder setSecrets(Map<String, String> secrets) {
            this.secrets = secrets;
            return this;
        }

        public Builder setRedirectUrls(List<String> redirectUrls) {
            this.redirectUrls = redirectUrls;
            return this;
        }

        public Builder setScopes(List<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        public Builder setQwac(String qwac) {
            this.qwac = qwac;
            return this;
        }

        public Builder setQsealc(String qsealc) {
            this.qsealc = qsealc;
            return this;
        }

        public SecretsEntityCore build() {
            SecretsEntityCore secretsEntityCore = new SecretsEntityCore();
            secretsEntityCore.secrets = this.secrets;
            secretsEntityCore.redirectUrls = this.redirectUrls;
            secretsEntityCore.scopes = this.scopes;
            secretsEntityCore.qwac = this.qwac;
            secretsEntityCore.qsealc = this.qsealc;
            return secretsEntityCore;
        }
    }
}
