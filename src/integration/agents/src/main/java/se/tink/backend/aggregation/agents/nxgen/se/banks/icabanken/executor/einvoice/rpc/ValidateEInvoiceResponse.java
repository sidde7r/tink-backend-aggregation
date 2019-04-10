package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.einvoice.entities.EmptyBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateEInvoiceResponse extends BaseResponse<EmptyBodyEntity> {

    private boolean isDateInvalidButChanged() {
        ResponseStatusEntity responseStatus = getResponseStatus();
        // The response below will also return HTTP 409. I feel like the check below is specific
        // enough.
        // Simply relying on the server message is a bit too vague. Looks at client message too.
        return responseStatus.getCode() == IcaBankenConstants.Transfers.EINVOICE_VALIDATE_ERROR_CODE
                && IcaBankenConstants.Transfers.EINVOICE_VALIDATE_ERROR_MSG.equalsIgnoreCase(
                        responseStatus.getServerMessage())
                && IcaBankenConstants.Transfers.EINVOICE_DATE_CHANGED_MSG.equalsIgnoreCase(
                        responseStatus.getClientMessage());
    }

    public boolean dateInvalidButIcaBankenCorrectedIt() {
        return isDateInvalidButChanged();
    }

    public boolean isValidationError() {
        return getResponseStatus().getCode()
                != IcaBankenConstants.Transfers.EINVOICE_VALIDATE_SUCCESS_CODE;
    }
}
