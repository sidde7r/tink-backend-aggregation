package se.tink.backend.aggregation.nxgen.controllers.authentication;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

/**
 * In progressive authentication, carry the intermediate step and fields. Yet to see if we need to
 * carry Credential object or any data in it.
 */
public final class SupplementInformationRequester {

    private ImmutableList<Field> fields;
    private ThirdPartyAppAuthenticationPayload payload;
    private SupplementalWaitRequest supplementalWaitRequest;

    private SupplementInformationRequester() {}

    public static SupplementInformationRequester empty() {
        return new SupplementInformationRequester();
    }

    public Optional<ImmutableList<Field>> getFields() {
        return Optional.ofNullable(fields);
    }

    public Optional<ThirdPartyAppAuthenticationPayload> getThirdPartyAppPayload() {
        return Optional.ofNullable(payload);
    }

    public Optional<SupplementalWaitRequest> getSupplementalWaitRequest() {
        return Optional.ofNullable(supplementalWaitRequest);
    }

    public static class Builder {
        private ImmutableList<Field> fields;
        private ThirdPartyAppAuthenticationPayload thirdPartyPayload;
        private SupplementalWaitRequest supplementalWaitRequest;

        public Builder withFields(List<Field> fields) {
            this.fields = ImmutableList.copyOf(fields);
            return this;
        }

        public Builder withThirdPartyAppAuthenticationPayload(
                ThirdPartyAppAuthenticationPayload payload) {
            this.thirdPartyPayload = payload;
            return this;
        }

        public Builder withSupplementalWaitRequest(
                SupplementalWaitRequest supplementalWaitRequest) {
            this.supplementalWaitRequest = supplementalWaitRequest;
            return this;
        }

        public SupplementInformationRequester build() {
            SupplementInformationRequester requester = new SupplementInformationRequester();
            requester.fields = fields;
            requester.payload = thirdPartyPayload;
            requester.supplementalWaitRequest = supplementalWaitRequest;
            return requester;
        }
    }
}
