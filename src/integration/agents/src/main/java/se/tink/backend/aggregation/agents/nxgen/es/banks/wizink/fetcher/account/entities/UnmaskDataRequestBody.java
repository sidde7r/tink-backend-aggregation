package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.OtpEntity;

public class UnmaskDataRequestBody {

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("otp")
    private OtpEntity otpEntity;

    public UnmaskDataRequestBody() {}

    private UnmaskDataRequestBody(Builder builder) {
        this.otpEntity = builder.otpEntity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OtpEntity otpEntity;

        public Builder otpEntity(OtpEntity otpEntity) {
            this.otpEntity = otpEntity;
            return this;
        }

        public UnmaskDataRequestBody build() {
            return new UnmaskDataRequestBody(this);
        }
    }
}
