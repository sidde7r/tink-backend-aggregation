package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants;

public abstract class UkOpenBankingV11Constants extends UkOpenBankingConstants {

    // Version 1.1 of the PIS api does not allow other currencies than this one.
    public static final String PIS_CURRENCY = "gbp";

    public static class Links {
        public static final String NEXT = "Next";
    }
}
