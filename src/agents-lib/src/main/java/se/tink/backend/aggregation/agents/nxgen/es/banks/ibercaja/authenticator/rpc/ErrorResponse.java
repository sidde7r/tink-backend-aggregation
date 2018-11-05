package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private static final Logger logger = LoggerFactory.getLogger(ErrorResponse.class);
    @JsonProperty("Numero")
    private int number;
    @JsonProperty("Descripcion")
    private String description;

    public void logError() {

        if (number != IberCajaConstants.ErrorCodes.INCORRECT_USERNAME_PASSWORD) {
            logger.info(String.format("UNKNOWN ERROR %d %s", number, description));
        }
    }
}
