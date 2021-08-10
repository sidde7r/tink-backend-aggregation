package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentErrorResponse {
    private List<TppMessageEntity> tppMessages;

    // "tppMessages":[{"text":"Invalid iban: ***ibans***","code":"FORMAT_ERROR","category":"ERROR"}]
    public boolean isIbanFormatError() {
        if (isNotOnlyOneTppMessage()) {
            return false;
        }

        return tppMessages.get(0).isFormatError();
    }

    // "tppMessages":[{"text":"Access to IBAN [***ibans***] is not allowed",
    // "code":"PRODUCT_INVALID","category":"ERROR"}]
    public boolean isProductInvalidError() {
        if (isNotOnlyOneTppMessage()) {
            return false;
        }

        return tppMessages.get(0).isInvalidProduct();
    }

    // More than one error in error list could imply other issue
    private boolean isNotOnlyOneTppMessage() {
        return tppMessages == null || tppMessages.size() != 1;
    }
}
