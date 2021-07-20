package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;

public enum SibsAccountSegment {
    PERSONAL,
    BUSINESS;

    static SibsAccountSegment getSegment(String selectedValue) {
        if (StringUtils.isNumeric(selectedValue)) {
            int index = Integer.parseInt(selectedValue) - 1;
            if (index >= 0 && index < SibsAccountSegment.values().length) {
                return SibsAccountSegment.values()[index];
            }
        }
        throw SupplementalInfoError.NO_VALID_CODE.exception(
                "Could not map user input to list of available options.");
    }
}
