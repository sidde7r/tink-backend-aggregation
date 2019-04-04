package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class GetSessionForm extends AbstractForm {
    private GetSessionForm(String auth, String service) {
        put(NordnetConstants.FormKeys.AUTH, auth);
        put(NordnetConstants.FormKeys.SERVICE, service);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String auth;
        private String service;

        public Builder setAuth(String auth) {
            this.auth = auth;
            return this;
        }

        public Builder setService(String service) {
            this.service = service;
            return this;
        }

        public GetSessionForm build() {
            return new GetSessionForm(this.auth, this.service);
        }
    }
}
