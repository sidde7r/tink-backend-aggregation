package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.errorhandling;

import lombok.experimental.UtilityClass;

@UtilityClass
class PolishApiErrors {
    static final String DAILY_REQUEST_LIMIT_REACHED = "TPP daily requests limit reached";
    static final String NOT_IMPLEMENTED = "not implemented";
    static final String DAYS_EN_90 = "90 days";
    static final String DAYS_PL_90 = "90 dni";
}
