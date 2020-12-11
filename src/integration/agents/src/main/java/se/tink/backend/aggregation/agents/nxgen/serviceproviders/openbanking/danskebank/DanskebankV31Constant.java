package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import com.google.common.collect.ImmutableList;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

import javax.annotation.Nonnull;
import java.util.List;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

public class DanskebankV31Constant {

    public static final List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code>
            ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS = ImmutableList.of(BBAN, IBAN);

    public static class Url {

        public static class V31 {
            public static final String AIS_BASE = "https://psd2-api.danskebank.com/psd2/v3.1/aisp";
            private static final String WELL_KNOWN =
                    "https://psd2-api.danskebank.com/psd2/{MARKET}/private/.well-known/openid-configuration";
            private static final String WELL_KNOWN_BUISNESS =
                    "https://psd2-api.danskebank.com/psd2/{MARKET}/business/.well-known/openid-configuration";

            public static URL getWellKnownUrl(@Nonnull final MarketCode market) {

                return new URL(WELL_KNOWN).parameter("MARKET", market.name().toLowerCase());
            }

            public static URL getWellKnownBusinessUrl(@Nonnull final MarketCode market) {

                return new URL(WELL_KNOWN_BUISNESS)
                        .parameter("MARKET", market.name().toLowerCase());
            }
        }
    }

    public static class ErrorCode {
        public static final String UNEXPETED_ERROR = "UK.OBIE.UnexpectedError";
    }
}
