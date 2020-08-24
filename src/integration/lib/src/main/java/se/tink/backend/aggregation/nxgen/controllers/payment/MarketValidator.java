package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.libraries.enums.MarketCode;

public class MarketValidator {

    private static final String GB = MarketCode.GB.toString();
    private static final String FR = MarketCode.FR.toString();

    public static boolean isSourceAccountMandatory(String market) {
        boolean isMandatory = true;

        // For GB and FR markets source account is optional and for all other markets source account
        // is mandatory
        if (GB.equalsIgnoreCase(market) || FR.equalsIgnoreCase(market)) {
            isMandatory = false;
        }
        return isMandatory;
    }
}
