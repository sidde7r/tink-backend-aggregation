package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OrderBankIdResponse {

    private String orderRef;
    private String autoStartToken;
    private String collectUrl;
    private Error error;

    public String getOrderRef() {
        return orderRef;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public Error getError() {
        return error;
    }

    public boolean isError() {
        return Objects.nonNull(error)
                || Objects.isNull(collectUrl)
                || Strings.isNullOrEmpty(collectUrl)
                || Strings.isNullOrEmpty(orderRef);
    }

    public class Error {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public boolean isBankIdAlreadyInProgress() {
            return ErrorMessages.ALREADY_IN_PROGRESS.equalsIgnoreCase(code);
        }

        public boolean isInvalidSsn() {
            return ErrorMessages.INVALID_SSN.equalsIgnoreCase(message);
        }
    }
}
