package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants.URLS.Links;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ConfirmInfoResponse extends BaseResponse
        implements ExecutorExceptionResolver.Messageable {

    private String cfrmMeth;
    private String cfrmText;
    private String notification;

    @JsonIgnore
    @Override
    public String getStatus() {
        return String.valueOf(getResponseStatus());
    }

    @JsonIgnore
    public URL getConfirmationVerificationLink(ExecutorExceptionResolver exceptionResolver) {
        try {
            return findLink(Links.CONFIRM_SIGN);
        } catch (IllegalStateException ex) {
            throw exceptionResolver.asException(this);
        }
    }

    @JsonIgnore
    public boolean needsBankIdSign() {
        return Transfers.BANKID_SIGN_NEEDED.equalsIgnoreCase(cfrmMeth);
    }
}
