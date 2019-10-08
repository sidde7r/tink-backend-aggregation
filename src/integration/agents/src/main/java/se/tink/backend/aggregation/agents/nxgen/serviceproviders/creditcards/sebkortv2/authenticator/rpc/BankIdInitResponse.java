package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import com.google.common.base.Strings;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class BankIdInitResponse {
    private String orderRef;
    private String autoStartToken;
    private URL collectUrl;
    private Error error;

    public String getOrderRef() {
        return orderRef;
    }

    public URL getCollectUrl() {
        return collectUrl;
    }

    public Error getError() {
        return error;
    }

    public boolean isError() {
        return Objects.nonNull(error)
                || Objects.isNull(collectUrl)
                || Strings.isNullOrEmpty(collectUrl.toString())
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
            return SebKortConstants.ErrorCode.ALREADY_IN_PROGRESS.equalsIgnoreCase(getCode());
        }
    }
}
