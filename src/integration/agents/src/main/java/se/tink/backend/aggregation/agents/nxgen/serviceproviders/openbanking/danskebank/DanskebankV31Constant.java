package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.DANSKE_BANK_ACCOUNT_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.enums.MarketCode;

public class DanskebankV31Constant {

    private static final String MARKET = "MARKET";

    public static final List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code>
            ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS =
                    ImmutableList.of(DANSKE_BANK_ACCOUNT_NUMBER, IBAN);

    public static final List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code>
            ALLOWED_CREDIT_CARD_ACCOUNT_IDENTIFIERS = ImmutableList.of(IBAN, BBAN);

    public static class Url {

        public static class V31 {
            public static final String AIS_BASE = "https://psd2-api.danskebank.com/psd2/v3.1/aisp";
            public static final String PIS_BASE = "https://psd2-api.danskebank.com/psd2/v3.1/pisp";
            public static final String AIS_BASE_APP_REDIRECT =
                    "https://psd2-mb3.danskebank.com/psd2/v3.1/aisp";
            private static final String WELL_KNOWN =
                    "https://psd2-mb3.danskebank.com/psd2/{MARKET}/private/.well-known/openid-configuration";
            private static final String WELL_KNOWN_UK =
                    "https://psd2-api.danskebank.com/psd2/uk/private/.well-known/openid-configuration";
            private static final String WELL_KNOWN_BUSINESS =
                    "https://psd2-api.danskebank.com/psd2/{MARKET}/business/.well-known/openid-configuration";

            public static URL getWellKnownUrl(@Nonnull final MarketCode market) {
                if (market == MarketCode.UK) {
                    return new URL(WELL_KNOWN_UK);
                }
                return new URL(WELL_KNOWN).parameter(MARKET, market.name().toLowerCase());
            }

            public static URL getWellKnownBusinessUrl(@Nonnull final MarketCode market) {

                return new URL(WELL_KNOWN_BUSINESS).parameter(MARKET, market.name().toLowerCase());
            }

            public static URL getPisBaseUrl() {

                return new URL(PIS_BASE);
            }
        }
    }

    public static class ErrorCode {
        public static final String UNEXPETED_ERROR = "UK.OBIE.UnexpectedError";
    }
}
