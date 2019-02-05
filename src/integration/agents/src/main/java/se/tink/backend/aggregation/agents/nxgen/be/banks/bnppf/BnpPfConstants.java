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

    // BankTransactionCode values
    public static final String PMNT = "PMNT";
    public static final String CCRD = "CCRD";
    public static final String POSD = "POSD";
    public static final String RCDT = "RCDT";
    public static final String ESCT = "ESCT";
    public static final String ICDT = "ICDT";
    public static final String MCRD = "MCRD";
    public static final String POSP = "POSP";
    public static final String RDDT = "RDDT";
    public static final String PMDD = "PMDD";
    public static final String CWDL = "CWDL";
    public static final String STDO = "STDO";
    public static final String ACMT = "ACMT";
    public static final String MDOP = "MDOP";
    public static final String CHRG = "CHRG";
    public static final String MCOP = "MCOP";
    public static final String INTR = "INTR";
    public static final String CDPT = "CDPT";
    public static final String LDAS = "LDAS";
    public static final String MGLN = "MGLN";
    public static final String RIMB = "RIMB";
    public static final String CSLN = "CSLN";
    public static final String SECU = "SECU";
    public static final String SETT = "SETT";
    public static final String SUBS = "SUBS";
    public static final String DDWN = "DDWN";
    public static final String OPCL = "OPCL";
    public static final String ACCC = "ACCC";
    public static final String PRDD = "PRDD";

    // Description mapping constants
    public static final String CASH_DEPOSIT = "Cash Deposit";
    public static final String CASH_WITHDRAWAL = "Cash Withdrawal";
    public static final String AG_INSURANCE_ALPHA_CREDIT = "AG INSURANCE/ALPHA CREDIT";
    public static final String AG_INSURANCE_FORTIS = "AG Insurance/Fortis";
    public static final String SEPARATOR = " ";

    public enum DescriptionRule {
        ATMPOS_OR_OREMIT(
                transaction -> transaction.matches(PMNT, CCRD, POSD)
                        || transaction.matches(PMNT, MCRD, POSP),
                transaction -> transaction.atmpos().isPresent() ?
                        transaction.atmpos() : transaction.oremit()
        ),
        OCNM(
                transaction -> transaction.matches(PMNT, RCDT, ESCT)
                        || transaction.matches(PMNT, ICDT, ESCT),
                transaction -> transaction.ocnm()
        ),
        OCNM_OREMIT_1(
                transaction -> transaction.matches(PMNT, RDDT, PMDD),
                transaction -> transaction.ocnm()
                        .map(ocnm -> (ocnm.toLowerCase().contains(AG_INSURANCE_ALPHA_CREDIT.toLowerCase())
                                && transaction.oremit().isPresent()) ?
                                ocnm + SEPARATOR + transaction.oremit().get() : ocnm)
        ),
        OCNM_OREMIT_2(
                transaction -> transaction.matches(PMNT, ICDT, STDO),
                transaction -> transaction.ocnm()
                        .map(ocnm -> ocnm.toLowerCase().contains(AG_INSURANCE_FORTIS.toLowerCase())
                                && transaction.oremit().isPresent() ?
                                ocnm + SEPARATOR + transaction.oremit().get() : ocnm)
        ),
        CARD_CASH_WITHDRAWAL(
                transaction -> transaction.matches(PMNT, CCRD, CWDL),
                transaction -> Optional.of(CASH_WITHDRAWAL
                        + (transaction.atmpos().isPresent() ? SEPARATOR + transaction.atmpos().get() : ""))
        ),
        OREMIT(
                transaction -> transaction.matches(ACMT, MDOP, CHRG),
                transaction -> transaction.oremit()
        ),
        BTC(
                transaction -> transaction.matches(PMNT, MDOP, CHRG)
                        || transaction.matches(ACMT, MCOP, INTR)
                        || transaction.matches(SECU, SETT, SUBS),
                transaction -> transaction.btc()
        ),
        CARD_CASH_DEPOSIT(
                transaction -> transaction.matches(PMNT, CCRD, CDPT),
                transaction -> Optional.of(CASH_DEPOSIT)
        ),
        BTC_OREMIT(
                transaction -> transaction.matches(LDAS, MGLN, RIMB)
                        || transaction.matches(LDAS, CSLN, RIMB)
                        || transaction.matches(LDAS, CSLN, DDWN),
                transaction -> join(transaction.btc(), transaction.oremit())
        ),
        OCNM_OREMIT(
                transaction -> transaction.matches(ACMT, OPCL, ACCC)
                        || transaction.matches(PMNT, RDDT, PRDD),
                transaction -> join(transaction.ocnm(), transaction.oremit())
        );

        private static String nullToEmpty(Optional<String> string) {
            return string.isPresent() ? string.get() : "";
        }

        private static Optional<String> join(Optional<String> a, Optional<String> b) {
            return Optional.of((nullToEmpty(a) + SEPARATOR + nullToEmpty(b)).trim());
        }

        private final Predicate<Transaction> predicate;
        private final Function<Transaction, Optional<String>> function;

        DescriptionRule(Predicate<Transaction> predicate, Function<Transaction, Optional<String>> function) {
            this.predicate = predicate;
            this.function = function;
        }

        public boolean matches(Transaction transaction) {
            return predicate.test(transaction);
        }

        public Optional<String> apply(Transaction transaction) {
            return function.apply(transaction);
        }
    }

    public enum PayloadRule {
        CASH_DEPOSIT_PAYLOAD(
                transaction -> transaction.matches(PMNT, CCRD, CDPT),
                transaction -> Optional.of(CASH_DEPOSIT.toUpperCase())
        ),
        CASH_WITHDRAWAL_PAYLOAD(
                transaction -> transaction.matches(PMNT, CCRD, CWDL),
                transaction -> Optional.of(CASH_WITHDRAWAL.toUpperCase()
                        + (transaction.atmpos().isPresent() ? SEPARATOR + transaction.atmpos()
                        : transaction.ocnm().isPresent() ? SEPARATOR + transaction.ocnm() : "")));

        private final Predicate<Transaction> predicate;
        private final Function<Transaction, Optional<String>> function;

        PayloadRule(Predicate<Transaction> predicate, Function<Transaction, Optional<String>> function) {
            this.predicate = predicate;
            this.function = function;
        }

        public boolean matches(Transaction transaction) {
            return predicate.test(transaction);
        }

        public Optional<String> apply(Transaction transaction) {
            return function.apply(transaction);
        }
    }
}
