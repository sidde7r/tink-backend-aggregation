package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.filters.rpc;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private String error;

    public boolean isServerError() {
        return StringUtils.equals(error, BankdataConstants.ErrorMessages.SERVER_ERROR);
    }
}
