package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.google.api.client.util.Objects;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.EmptyBody;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.Response;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateEInvoiceResponse extends Response<EmptyBody> {

    private static final int NO_VALIDATION_ERROR_STATUS_CODE = 0;

    private boolean isDateInvalidButChanged() {
        // The response below will also return HTTP 409. I feel like the check below is specific enough.
        return Objects.equal(getResponseStatus().getCode(), 1000) && Objects.equal(
                getResponseStatus().getServerMessage(), "Error validating invoice.") &&
                // Simply relying on "Error validating invoice." is a bit too vague. Using the requirement below, too.
                Objects.equal(getResponseStatus().getClientMessage(),
                        "Angivet datum har ändrats till närmast möjliga dag.");
    }

    public boolean isInvalidButICABankenCorrectedIt() {
        return isDateInvalidButChanged();
    }

    public boolean isValidationError() {
        return getResponseStatus().getCode() != NO_VALIDATION_ERROR_STATUS_CODE;
    }
}
