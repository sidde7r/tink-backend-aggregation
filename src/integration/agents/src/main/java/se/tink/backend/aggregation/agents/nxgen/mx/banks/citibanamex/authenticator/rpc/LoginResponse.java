package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.entity.CertCellphoneInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    private String actionChange;
    private List<CertCellphoneInfoEntity> certCellphoneInfo;
    private String certCellphoneInfoStatus;
    private String clientActivityType;
    private String clientName;
    private String contractAccepted;
    private String isPriority;
    private String lastAccessChannel;
    private String lastAccessDate;
    private String mobileBankingStatus;
    private String operationTimestamp;
    private String registerStatus;
    private String userTyp;

    @JsonIgnore
    public LoginResponse handleErrors() throws LoginException {
        if (Errors.INCORRECT_PASSWORD.equalsIgnoreCase(faultstring)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (Errors.BANK_SIDE_ERROR.equalsIgnoreCase(faultstring)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Error message: " + faultstring);
        } else if (Errors.MULTIPLE_SESSION_ACTIVE.equalsIgnoreCase(faultstring)) {
            throw BankServiceError.MULTIPLE_LOGIN.exception("Error message: " + faultstring);
        } else if (StringUtils.isNotEmpty(faultstring)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Error message: " + faultstring);
        }
        return this;
    }

    public String getClientName() {
        return clientName;
    }
}
