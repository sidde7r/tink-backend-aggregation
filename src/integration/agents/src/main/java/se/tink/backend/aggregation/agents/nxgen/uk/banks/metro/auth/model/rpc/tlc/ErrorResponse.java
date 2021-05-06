package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.vavr.control.Option;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.ErrorResponse.DataEntity.DetailedErrorMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(value = SnakeCaseStrategy.class)
public class ErrorResponse {

    private int errorCode;

    private DataEntity data;

    @JsonIgnore
    public boolean hasErrorCode(int errorCode) {
        return this.errorCode == errorCode;
    }

    @JsonIgnore
    public boolean hasAssertionErrorCode(int errorCode) {
        return !Option.of(data).filter(e -> e.getAssertionErrorCode() == errorCode).isEmpty();
    }

    @JsonIgnore
    public boolean isAccountLocked() {
        return Option.of(data)
                .flatMap(DataEntity::getErrorDetails)
                .map(DetailedErrorMessage::isAccountLocked)
                .getOrElse(false);
    }

    @JsonObject
    @Getter
    @JsonNaming(value = SnakeCaseStrategy.class)
    static class DataEntity {
        private int assertionErrorCode;
        private String assertionErrorMessage;
        private DetailedErrorMessage data;

        private Option<DetailedErrorMessage> getErrorDetails() {
            return Option.of(data);
        }

        @JsonObject
        @Getter
        @JsonNaming(value = SnakeCaseStrategy.class)
        static class DetailedErrorMessage {
            private int retriesLeft;
            private boolean accountLocked;
        }
    }
}
