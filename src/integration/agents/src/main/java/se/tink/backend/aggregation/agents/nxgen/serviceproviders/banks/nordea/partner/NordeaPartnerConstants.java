package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.agents.rpc.Field;

public class NordeaPartnerConstants {

    public static final String INTEGRATION_NAME = "nordeapartner";

    public class StorageKeys {
        public static final String PARTNER_USER_ID = "partner-user-id";
        public static final String TOKEN = "token";
    }

    public class SupplementalInfoKeys {
        public static final String TOKEN = "token";
    }

    public class Jwt {
        public static final String ISSUER = "Tink";
        public static final String JWT_CONTENT_TYPE = "JWT";
        public static final long TOKEN_LIFETIME_SECONDS = 300;
    }

    public static class SupplementalFields {
        public static final Field TOKEN =
                Field.builder()
                        .name(SupplementalInfoKeys.TOKEN)
                        .description("Nordea JWE token")
                        .build();
    }
}
