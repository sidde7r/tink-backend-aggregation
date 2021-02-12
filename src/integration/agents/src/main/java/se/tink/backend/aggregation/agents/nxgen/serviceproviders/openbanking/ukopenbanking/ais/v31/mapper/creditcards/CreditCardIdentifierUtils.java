package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class CreditCardIdentifierUtils {

    static int getCardIdentifierLength(String cardIdentifier) {
        return StringUtils.isNotBlank(cardIdentifier)
                ? StringUtils.deleteWhitespace(cardIdentifier.replace("-", "")).length()
                : 0;
    }

    static String isMaskedIdentifier(String cardIdentifier) {
        if (StringUtils.isBlank(cardIdentifier)) {
            return "empty";
        }

        return Optional.of(cardIdentifier)
                .map(s -> s.replace("-", ""))
                .map(StringUtils::deleteWhitespace)
                .map(s -> !StringUtils.isNumeric(s))
                .map(Object::toString)
                .orElseGet(() -> "unknown");
    }
}
