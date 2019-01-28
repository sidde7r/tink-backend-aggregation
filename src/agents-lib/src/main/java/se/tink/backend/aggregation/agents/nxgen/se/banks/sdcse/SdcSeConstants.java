package se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.agents.models.TransactionTypes;

public class SdcSeConstants {

    public static class Market {
        public static final String SWEDEN = "SE";
        public static final String BASE_URL = "https://prod.smartse.sdc.dk/restapi/";
        public static final String PHONE_COUNTRY_CODE = "+46";
    }

    public static class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_sdc_se");
        public static final LogTag INVESTMENTS_LOGGING = LogTag.from("#investment_logging_sdc_se");
    }

    // do we really need this?
    public enum SdcSeTransactionType {
        CREDIT_CARD        ("Kortköp ", 8, TransactionTypes.CREDIT_CARD),
        CASH_WITHDRAWAL    ("Kontantuttag ", 13, TransactionTypes.WITHDRAWAL),
        AUTOGIRO           ("Autogiro ", 9, TransactionTypes.PAYMENT),
        DEPOSIT_OTHER_BANK ("Insättning från annan bank ", 27, TransactionTypes.DEFAULT),
        DEPOSIT            ("Insättning ", 11, TransactionTypes.DEFAULT),
        CHARGEBACK         ("Återbetalning ", 14, TransactionTypes.DEFAULT);

        private final String label;
        private final int length;
        private final TransactionTypes type;

        SdcSeTransactionType(String label, int length, TransactionTypes type) {
            this.label = label;
            this.length = length;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public int getLength() {
            return length;
        }

        public TransactionTypes getType() {
            return type;
        }
    }
}
