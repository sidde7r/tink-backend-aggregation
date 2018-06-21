package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities.Transaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;

public class BnpPfConstants {
    public static final String CURRENCY = "EUR";

    public enum Url implements UrlEnum {
        PFMP_PREFERENCES(createUrlWithHost("/pfm-preferences/v2/pfm-preferences?tokenized=false")),
        TRANSACTIONS(createUrlWithHost("/cash-account/account-transaction/v1/accounts/{accountId}{currency}/transactions"));

        private URL url;

        Url(String url) {
            this.url = new URL(url);
        }

        @Override
        public URL get() {
            return url;
        }

        @Override
        public URL parameter(String key, String value) {
            return url.parameter(key, value);
        }

        @Override
        public URL queryParam(String key, String value) {
            return url.queryParam(key, value);
        }

        public static final String HOST = "https://api.qabnpparibasfortis.be";

        private static String createUrlWithHost(String uri) {
            return HOST + uri;
        }
    }

    public static final String PENDING = "Pending";

}
