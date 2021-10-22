package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LaBanquePostaleErrorResponse {

    private static final Set<String> ERRRORS =
            Sets.newHashSet(
                    "Une erreur technique est survenue",
                    "This service is temporarily unavailable. Please try again later.",
                    "Exception - Solde comptable et operationnel null");

    private int status;
    private String message;

    public boolean isBankSideError() {
        return this.status == 500
                && ERRRORS.stream().anyMatch(error -> this.message.contains(error));
    }
}
