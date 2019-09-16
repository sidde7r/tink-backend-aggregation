package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers.Statuses;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ConfirmTransferResponse extends BaseResponse
        implements ExecutorExceptionResolver.Messageable {

    private String result;

    @JsonIgnore
    @Override
    public String getStatus() {
        return result;
    }

    @JsonIgnore
    public void validateResult(ExecutorExceptionResolver exceptionResolver) {
        if (!Statuses.SIGN_CONFIRMED.equalsIgnoreCase(result)) {
            throw exceptionResolver.asException(this);
        }
    }

    @JsonIgnore
    public URL getConfirmExecuteLink(ExecutorExceptionResolver exceptionResolver) {
        try {
            return findLink(Links.CONFIRM_EXECUTE);
        } catch (IllegalStateException ex) {
            throw exceptionResolver.asException(this);
        }
    }

    @JsonIgnore
    public BankIdStatus getBankIdStatus() {
        if (Strings.isNullOrEmpty(result)) {
            return checkCodeForReason();
        }

        switch (result.toUpperCase()) {
            case Statuses.CONTINUE:
                return BankIdStatus.WAITING;
            case Statuses.SIGN_CONFIRMED:
                return BankIdStatus.DONE;
            default:
                return BankIdStatus.FAILED_UNKNOWN;
        }

    }

    private BankIdStatus checkCodeForReason() {
        String errorCode = getCode();

        if (!Strings.isNullOrEmpty(errorCode)
                && Statuses.CANCELLED.equalsIgnoreCase(errorCode)) {
            return BankIdStatus.CANCELLED;
        }

        return BankIdStatus.FAILED_UNKNOWN;
    }
}
