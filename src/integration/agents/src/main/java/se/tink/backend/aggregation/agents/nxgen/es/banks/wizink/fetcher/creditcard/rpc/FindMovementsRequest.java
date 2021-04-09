package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.OtpRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FindMovementsRequest {

    public FindMovementsRequest(String accountNumber, OtpRequest otp, String dateFrom) {
        this.requestBody = new FindMovementsRequestBody(accountNumber, false, otp, dateFrom);
    }

    public FindMovementsRequest(String accountNumber, boolean needOtp, String dateFrom) {
        this.requestBody = new FindMovementsRequestBody(accountNumber, needOtp, null, dateFrom);
    }

    @JsonProperty("FindMovementsRequest")
    public FindMovementsRequestBody requestBody;

    @JsonObject
    private static class FindMovementsRequestBody {
        private String accountNumber;
        private boolean needOtp;

        @JsonInclude(Include.NON_NULL)
        private OtpRequest otp;

        private String dateFrom;

        private FindMovementsRequestBody(
                String accountNumber, boolean needOtp, OtpRequest otp, String dateFrom) {
            this.accountNumber = accountNumber;
            this.needOtp = needOtp;
            this.otp = otp;
            this.dateFrom = dateFrom;
        }
    }
}
