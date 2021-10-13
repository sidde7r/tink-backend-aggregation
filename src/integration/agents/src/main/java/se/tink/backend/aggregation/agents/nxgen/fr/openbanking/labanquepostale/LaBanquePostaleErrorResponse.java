package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LaBanquePostaleErrorResponse {

    private int status;
    private String message;

    public boolean isBankSideError() {
        return this.status == 500
                && (this.message.contains("Une erreur technique est survenue")
                        || this.message.contains(
                                "This service is temporarily unavailable. Please try again later."));
    }
}
