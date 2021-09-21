package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@JsonObject
public class ErrorResponse {
    private String type;
    private String title;
    private String code;

    @JsonIgnore
    public boolean isNotSubscribedError(int code) {
        return code == HttpStatus.SC_UNAUTHORIZED
                && title.trim().equalsIgnoreCase(RabobankConstants.ErrorMessages.NOT_SUBSCRIBED);
    }

    @JsonIgnore
    public boolean isPeriodInvalidError(HttpResponseException e) {
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(e.getResponse().getType())) {
            log.info("Invalid response body: {}", e.getResponse().getBody(String.class));
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        return code.equals(RabobankConstants.ErrorCodes.PERIOD_INVALID);
    }
}
