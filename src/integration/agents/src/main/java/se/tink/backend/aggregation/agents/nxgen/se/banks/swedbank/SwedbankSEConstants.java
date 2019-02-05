package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ProfileParameters;

public class SwedbankSEConstants {
/*
    public static final String API_KEY = "Rh9KYIhL11Nn2XoR";
    public static final String BANK_ID = "08999";
    public static final String BANK_NAME = "Swedbank AB (publ)";
*/
    public static final String LOAN_YEARS = "år";
    public static final String LOAN_MONTHS = "mån";
    public static final String MEMBERSHIP_LOAN = "Medlemslån";
    public static final String AMORTIZATION = "Amorteringsbelopp";

    public static final ImmutableMap<String, ProfileParameters> PROFILE_PARAMETERS =
            new ImmutableMap.Builder<String, ProfileParameters>()
                    .put("swedbank",
                            new ProfileParameters("swedbank", "rMKD7LKhhFNVOXJK", false))
                    .put("swedbank-youth",
                            new ProfileParameters("swedbank-youth", "ap4TcWEoEGV42UVn", false))
                    .put("savingsbank",
                            new ProfileParameters("savingsbank", "CB2PGrGdDIJKcrRd", true))
                    .put("savingsbank-youth",
                            new ProfileParameters("savingsbank-youth", "LFQP9KuzqNBJOosw", true))
                    .build();

}
