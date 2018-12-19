package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BecErrorResponse {
    private String action;
    private String message;

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return Strings.isNullOrEmpty(message) ? "" : message.toLowerCase();
    }

    public boolean isWithoutMortgage() {
        return getMessage().contains(BecConstants.ErrorMessage.NO_MORTGAGE);
    }

    public boolean noDetailsExist() {
        return getMessage().contains(BecConstants.ErrorMessage.LOAN_NO_DETAILS_EXIST);
    }
}
