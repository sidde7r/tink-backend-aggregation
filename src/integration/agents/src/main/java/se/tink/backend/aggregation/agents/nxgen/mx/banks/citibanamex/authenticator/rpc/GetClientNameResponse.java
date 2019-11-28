package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetClientNameResponse extends BaseResponse {
    private String clientName;
    private String mobileBankingStatus;
    private String registerStatus;
    private String nextAction;

    @JsonIgnore
    public GetClientNameResponse handleErrors() throws LoginException {
        if (Errors.INCORRECT_CLIENT_NAME.equalsIgnoreCase(faultstring)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (Errors.BANK_SIDE_ERROR.equalsIgnoreCase(faultstring)
                || StringUtils.isNotEmpty(faultstring)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Error message: " + faultstring);
        }
        return this;
    }
}
