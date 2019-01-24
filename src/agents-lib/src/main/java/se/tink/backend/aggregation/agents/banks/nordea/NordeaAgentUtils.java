package se.tink.backend.aggregation.agents.banks.nordea;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.PaymentEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Details;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class NordeaAgentUtils {
    protected static final Map<String, String> ACCOUNT_NAMES_BY_CODE = Maps.newHashMap();
    protected static final Map<String, AccountTypes> ACCOUNT_TYPES_BY_CODE = Maps.newHashMap();
    protected static final Map<String, AccountTypes> ACCOUNT_TYPES_BY_NAME = Maps.newHashMap();
    private static final Map<String, Loan.Type> LOAN_TYPES_BY_CODE = Maps.newHashMap();
    protected static final Splitter CLEANUP_SPLITTER = Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings();
    protected static final Joiner CLEANUP_JOINER = Joiner.on(' ');

    /**
     * Standard transaction ordering based on date and inserted.
     */
    public static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getDescription(), right.getDescription()).compare(left.getId(), right.getId())
                    .result();
        }
    };

    public static final Function<PaymentEntity, Transaction> PAYMENT_TO_TRANSACTION =
            PaymentEntity::toTransaction;

    public static final Predicate<ProductEntity> PRODUCT_CAN_MAKE_PAYMENT =
            productEntity -> productEntity != null && productEntity.canMakePayment();

    public static final Function<ProductEntity, String> PRODUCT_TO_INTERNAL_ID =
            new Function<ProductEntity, String>() {
                @Nullable
                @Override
                public String apply(@Nullable ProductEntity productEntity) {
                    if (productEntity == null) {
                        return null;
                    }

                    return productEntity.getInternalId();
                }
            };

    public static final Function<ProductEntity, String> PRODUCT_TO_FROM_ACCOUNT_ID =
            new Function<ProductEntity, String>() {
                @Nullable
                @Override
                public String apply(@Nullable ProductEntity productEntity) {
                    if (productEntity == null) {
                        return null;
                    }

                    return productEntity.getAccountId();
                }
            };

    static {
        addAccountType("SE240", "Nordea Premium", AccountTypes.CREDIT_CARD);
        addAccountType("SE002", "Bankkort MasterCard", AccountTypes.CREDIT_CARD);
        addAccountType("SE005", "Nordea Debit", AccountTypes.CHECKING);
        addAccountType("SE003", "Nordea Debit", AccountTypes.CHECKING);
        addAccountType("SE006", "Företagskort MasterCard", AccountTypes.CREDIT_CARD);
        addAccountType("SE004", "Nordea Electron", AccountTypes.CREDIT_CARD);
        addAccountType("SE100", "Visa Gold", AccountTypes.CREDIT_CARD);
        addAccountType("SE010", "Visa Gold", AccountTypes.CREDIT_CARD);
        addAccountType("SE200", "Nordea Credit", AccountTypes.CREDIT_CARD);
        addAccountType("SE210", "Nordea Platinum", AccountTypes.CREDIT_CARD);
        addAccountType("SE009", "Nordea Platinum", AccountTypes.CREDIT_CARD);
        addAccountType("SE230", "Nordea Black", AccountTypes.CREDIT_CARD);
        addAccountType("SE260", "Nordea Gold", AccountTypes.CREDIT_CARD);

        addAccountType("SE0000", "Personkonto", AccountTypes.CHECKING);
        addAccountType("SE0001", "Fn-konto", AccountTypes.CHECKING);
        addAccountType("SE0002", "Ombudskonto", AccountTypes.CHECKING);
        addAccountType("SE0004", "Synskadekonto", AccountTypes.CHECKING);
        addAccountType("SE0029", "Företagskonto", AccountTypes.CHECKING);
        addAccountType("SE0005", "Utlandslönekonto", AccountTypes.CHECKING);
        addAccountType("SE1101", "Private Bankingkonto", AccountTypes.CHECKING);
        addAccountType("SE0100", "Personkonto Solo", AccountTypes.CHECKING);
        addAccountType("SE0101", "Fn-konto Solo", AccountTypes.CHECKING);
        addAccountType("SE0104", "Synskadekonto Solo", AccountTypes.CHECKING);
        addAccountType("SE0105", "Utlandslönekto Solo", AccountTypes.CHECKING);
        addAccountType("SE0200", "Personkonto-student", AccountTypes.CHECKING);
        addAccountType("SE0300", "Personkonto-ungdom", AccountTypes.CHECKING);
        addAccountType("SE0302", "Ombudskonto", AccountTypes.CHECKING);
        addAccountType("SE0304", "Synskadekonto", AccountTypes.CHECKING);
        addAccountType("SE0402", "IPS cash account", AccountTypes.CHECKING);
        addAccountType("SE0500", "Depålikvidkonto", AccountTypes.CHECKING);
        addAccountType("SE0502", "Ombudskonto", AccountTypes.CHECKING);
        addAccountType("SE0600", "Pgkonto Privat", AccountTypes.CHECKING);
        addAccountType("SE0606", "Eplusgiro Privat", AccountTypes.CHECKING);
        addAccountType("SE0700", "Pgkonto Privat Ftg", AccountTypes.CHECKING);
        addAccountType("SE0706", "Eplusgiro Privat Ftg", AccountTypes.CHECKING);
        addAccountType("SE0900", "Flexkonto", AccountTypes.CHECKING);

        addAccountType("SE1000", "Checkkonto", AccountTypes.CHECKING);
        addAccountType("SE1001", "Pensionskredit", AccountTypes.CHECKING);
        addAccountType("SE1002", "Boflex", AccountTypes.CHECKING);
        addAccountType("SE1003", "Boflex pension", AccountTypes.CHECKING);
        addAccountType("SE1004", "Buffertkonto kredit", AccountTypes.CHECKING);
        addAccountType("SE1100", "Aktielikvidkonto", AccountTypes.CHECKING);
        addAccountType("SE1200", "Externt Konto", AccountTypes.CHECKING);
        addAccountType("SE1211", "Zb Ext Top", AccountTypes.CHECKING);
        addAccountType("SE1212", "Zb Externt Sub Gr Lvl", AccountTypes.CHECKING);
        addAccountType("SE1213", "Zb Externt Sub Acc", AccountTypes.CHECKING);
        addAccountType("SE1214", "Zb Externt Adj Acc", AccountTypes.CHECKING);
        addAccountType("SE1300", "Cross-Border Account", AccountTypes.CHECKING);
        addAccountType("SE1311", "Zb Cross-Border Top", AccountTypes.CHECKING);
        addAccountType("SE1312", "Zb C-B Sub Gr Level", AccountTypes.CHECKING);
        addAccountType("SE1313", "Zb C-B Sub Account", AccountTypes.CHECKING);
        addAccountType("SE1314", "Zb Cross-Border Adj", AccountTypes.CHECKING);
        addAccountType("SE1400", "Föreningskonto", AccountTypes.CHECKING);
        addAccountType("SE1411", "Zb Föreningskonto Top", AccountTypes.CHECKING);
        addAccountType("SE1412", "Zb Före.Kto Sub Gr Level", AccountTypes.CHECKING);
        addAccountType("SE1413", "Zb Föreningskonto Sub Acc", AccountTypes.CHECKING);
        addAccountType("SE1414", "Zb Föreningskonto Adj Acc", AccountTypes.CHECKING);
        addAccountType("SE1500", "Församlingskonto", AccountTypes.CHECKING);
        addAccountType("SE1600", "Baskonto", AccountTypes.CHECKING);
        addAccountType("SE1700", "Direktkonto", AccountTypes.CHECKING);
        addAccountType("SE1701", "Affärskonto", AccountTypes.CHECKING);
        addAccountType("SE1711", "Zb Direktkonto Top", AccountTypes.CHECKING);
        addAccountType("SE1712", "Zb Dir.Konto Sub Gr Level", AccountTypes.CHECKING);
        addAccountType("SE1713", "Zb Dir.Konto Sub Account", AccountTypes.CHECKING);
        addAccountType("SE1714", "Zb Dir.Konto Adj Account", AccountTypes.CHECKING);
        addAccountType("SE1800", "Företagskonto", AccountTypes.CHECKING);
        addAccountType("SE1801", "Top Account", AccountTypes.CHECKING);
        addAccountType("SE1802", "Sub Group Level", AccountTypes.CHECKING);
        addAccountType("SE1803", "Sub Account", AccountTypes.CHECKING);
        addAccountType("SE1804", "Adjustement Account", AccountTypes.CHECKING);
        addAccountType("SE1811", "Zb Företagskonto Top", AccountTypes.CHECKING);
        addAccountType("SE1812", "Zb Ftgskonto Sub Gr Level", AccountTypes.CHECKING);
        addAccountType("SE1813", "Zb Företagskonto Sub Acc", AccountTypes.CHECKING);
        addAccountType("SE1814", "Zb Företagskonto Adj Acc", AccountTypes.CHECKING);
        addAccountType("SE1900", "Arbetsgivarkonto", AccountTypes.CHECKING);
        addAccountType("SE1901", "Avdragsmottagarkonto", AccountTypes.CHECKING);
        addAccountType("SE1902", "Floatkonto", AccountTypes.CHECKING);

        addAccountType("SE2000", "Girokapital Kfm", AccountTypes.CHECKING);
        addAccountType("SE2100", "Specialkonto", AccountTypes.CHECKING);
        addAccountType("SE2200", "Sparkonto Företag", AccountTypes.CHECKING);
        addAccountType("SE2300", "Koncern Plusgirokonto", AccountTypes.CHECKING);
        addAccountType("SE2311", "Koncern Toppkonto", AccountTypes.CHECKING);
        addAccountType("SE2312", "Koncern Samlingskto", AccountTypes.CHECKING);
        addAccountType("SE2313", "Koncern Transkonto", AccountTypes.CHECKING);
        addAccountType("SE2400", "Myndighetskonto", AccountTypes.CHECKING);
        addAccountType("SE2405", "Myndighetskonto", AccountTypes.CHECKING);
        addAccountType("SE2411", "Myndighet Toppkonto", AccountTypes.CHECKING);
        addAccountType("SE2412", "Myndighet Samlingskto", AccountTypes.CHECKING);
        addAccountType("SE2413", "Myndighet Transkonto", AccountTypes.CHECKING);
        addAccountType("SE2499", "Avsl Myndighetskonto", AccountTypes.CHECKING);
        addAccountType("SE2500", "Plusgirokonto Nordea", AccountTypes.CHECKING);
        addAccountType("SE2700", "Internt Arbetskonto", AccountTypes.CHECKING);
        addAccountType("SE2900", "Plusgirokonto", AccountTypes.CHECKING);

        addAccountType("SE3000", "Nordea Liv", AccountTypes.INVESTMENT);
        addAccountType("SE3201", "Allemansfond Alfa", AccountTypes.INVESTMENT);
        addAccountType("SE3202", "Allemansfond Beta", AccountTypes.INVESTMENT);
        addAccountType("SE3203", "Allemansfond Gamma", AccountTypes.INVESTMENT);
        addAccountType("SE3204", "Allemansfond Olympia", AccountTypes.INVESTMENT);
        addAccountType("SE3205", "Allemansfond Omega", AccountTypes.INVESTMENT);
        addAccountType("SE3206", "Allemansfond Trade", AccountTypes.INVESTMENT);
        addAccountType("SE3207", "Allemansfond Trust", AccountTypes.INVESTMENT);
        addAccountType("SE3208", "North American Value Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3209", "European Value Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3210", "Spara Ettan", AccountTypes.INVESTMENT);
        addAccountType("SE3211", "Spara Trean", AccountTypes.INVESTMENT);
        addAccountType("SE3212", "Spara Femman", AccountTypes.INVESTMENT);
        addAccountType("SE3213", "Spara Premiepension", AccountTypes.INVESTMENT);
        addAccountType("SE3214", "Nordea Realräntefond", AccountTypes.INVESTMENT);
        addAccountType("SE3215", "Nordea Stratega 70", AccountTypes.INVESTMENT);
        addAccountType("SE3216", "Nordea Stratega 50", AccountTypes.INVESTMENT);
        addAccountType("SE3217", "Nordea Stratega 10", AccountTypes.INVESTMENT);
        addAccountType("SE3218", "Inst Aktiefond Europa", AccountTypes.INVESTMENT);
        addAccountType("SE3219", "Inst Penningmarknad", AccountTypes.INVESTMENT);
        addAccountType("SE3220", "Inst Aktiefonden Sverige", AccountTypes.INVESTMENT);
        addAccountType("SE3221", "Inst Aktiefonden Världen", AccountTypes.INVESTMENT);
        addAccountType("SE3222", "Inst Räntefonden Korta P", AccountTypes.INVESTMENT);
        addAccountType("SE3223", "Inst Räntefonden Långa P", AccountTypes.INVESTMENT);
        addAccountType("SE3224", "Småb.Norden", AccountTypes.INVESTMENT);
        addAccountType("SE3225", "Portföljinv. Global", AccountTypes.INVESTMENT);
        addAccountType("SE3226", "Ncc Allemansfond", AccountTypes.INVESTMENT);
        addAccountType("SE3228", "Akzo Nobel Aktiefond", AccountTypes.INVESTMENT);
        addAccountType("SE3229", "Nobel Allemansfond", AccountTypes.INVESTMENT);
        addAccountType("SE3230", "Tillväxtbolagsfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3231", "Global Mobility", AccountTypes.INVESTMENT);
        addAccountType("SE3232", "Selekta Tillväxt", AccountTypes.INVESTMENT);
        addAccountType("SE3233", "Global Biotech", AccountTypes.INVESTMENT);
        addAccountType("SE3234", "Latinamerikafonden", AccountTypes.INVESTMENT);
        addAccountType("SE3235", "Foresta", AccountTypes.INVESTMENT);
        addAccountType("SE3236", "Borgen", AccountTypes.INVESTMENT);
        addAccountType("SE3237", "Förvaltningsfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3239", "Euroland", AccountTypes.INVESTMENT);
        addAccountType("SE3240", "Futura", AccountTypes.INVESTMENT);
        addAccountType("SE3241", "Optima", AccountTypes.INVESTMENT);
        addAccountType("SE3242", "Fjärran Östern", AccountTypes.INVESTMENT);
        addAccountType("SE3243", "Selekta Europa", AccountTypes.INVESTMENT);
        addAccountType("SE3244", "Selekta Sverige", AccountTypes.INVESTMENT);
        addAccountType("SE3245", "Östeuropa", AccountTypes.INVESTMENT);
        addAccountType("SE3246", "Far Eastern Value Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3247", "Pro Europa", AccountTypes.INVESTMENT);
        addAccountType("SE3248", "Pro Europa Ränta", AccountTypes.INVESTMENT);
        addAccountType("SE3249", "Donationsmedelsfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3250", "Pro Corporate Bond", AccountTypes.INVESTMENT);
        addAccountType("SE3251", "Sverigefonden", AccountTypes.INVESTMENT);
        addAccountType("SE3252", "Spektra", AccountTypes.INVESTMENT);
        addAccountType("SE3253", "Avanti", AccountTypes.INVESTMENT);
        addAccountType("SE3254", "Japanfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3255", "Nordamerikafonden", AccountTypes.INVESTMENT);
        addAccountType("SE3256", "Nordenfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3257", "Europafonden", AccountTypes.INVESTMENT);
        addAccountType("SE3258", "Selekta Nordamerika", AccountTypes.INVESTMENT);
        addAccountType("SE3259", "Global Value Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3260", "Sweden Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3261", "European Mid Cap", AccountTypes.INVESTMENT);
        addAccountType("SE3262", "Euro Property Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3263", "Mediterranean Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3264", "Asian Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3265", "Us Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3266", "Avtalspension Mini", AccountTypes.INVESTMENT);
        addAccountType("SE3267", "Avtalspension Midi", AccountTypes.INVESTMENT);
        addAccountType("SE3268", "Avtalspension Maxi", AccountTypes.INVESTMENT);
        addAccountType("SE3269", "Portföljinv. Sverige", AccountTypes.INVESTMENT);
        addAccountType("SE3270", "Medica-Life Science", AccountTypes.INVESTMENT);
        addAccountType("SE3271", "Itfond", AccountTypes.INVESTMENT);
        addAccountType("SE3272", "Obligationsfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3273", "Euro Obligation", AccountTypes.INVESTMENT);
        addAccountType("SE3274", "Portföljinvest Obl", AccountTypes.INVESTMENT);
        addAccountType("SE3275", "Obligationsinvest", AccountTypes.INVESTMENT);
        addAccountType("SE3276", "International Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3277", "Sekura", AccountTypes.INVESTMENT);
        addAccountType("SE3278", "Euro Ränta", AccountTypes.INVESTMENT);
        addAccountType("SE3279", "Likviditetsinvest", AccountTypes.INVESTMENT);
        addAccountType("SE3280", "Japanese Value Fund", AccountTypes.INVESTMENT);
        addAccountType("SE3281", "Räntefonden", AccountTypes.INVESTMENT);
        addAccountType("SE3282", "Likviditetsfonden", AccountTypes.INVESTMENT);
        addAccountType("SE3286", "Svenska Portfölj", AccountTypes.INVESTMENT);
        addAccountType("SE3287", "Internation Portfölj", AccountTypes.INVESTMENT);
        addAccountType("SE3290", "Premiepension 1938-44", AccountTypes.INVESTMENT);
        addAccountType("SE3291", "Premiepension 1945-49", AccountTypes.INVESTMENT);
        addAccountType("SE3292", "Premiepension 1950-54", AccountTypes.INVESTMENT);
        addAccountType("SE3293", "Premiepension 1955-59", AccountTypes.INVESTMENT);
        addAccountType("SE3294", "Premiepension 1960-64", AccountTypes.INVESTMENT);
        addAccountType("SE3295", "Premiepension 1965-69", AccountTypes.INVESTMENT);
        addAccountType("SE3296", "Premiepension 1970-74", AccountTypes.INVESTMENT);
        addAccountType("SE3297", "Premiepension 1975-79", AccountTypes.INVESTMENT);
        addAccountType("SE3298", "Premiepension 1980-84", AccountTypes.INVESTMENT);
        addAccountType("SE3299", "European Hedge", AccountTypes.INVESTMENT);
        addAccountType("SE3800", "Vinnarkonto", AccountTypes.INVESTMENT);
        addAccountType("SE3900", "Återvinningskonto", AccountTypes.INVESTMENT);
        addAccountType("SE500", "Depålikvidkonto", AccountTypes.INVESTMENT);

        addAccountType("SE4000", "Spara Kapital", AccountTypes.SAVINGS);
        addAccountType("SE4300", "ISK Classic likvidkonto", AccountTypes.SAVINGS);
        // NOTE: There is no hit for SE4309, since the description of SE4300 changed (2.8.0), guess it might be a
        // mistake that SE4309 is actually SE4300.
        addAccountType("SE0037", "Boflex Pension", AccountTypes.PENSION);
        addAccountType("SE0052", "ISK Depålikvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE0059", "Aktielikvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE1102", "ISK likvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE1109", "Sparkonto", AccountTypes.PENSION);
        addAccountType("SE4309", "ISK Classic likvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE4400", "Skatteutjämningskonto", AccountTypes.SAVINGS);
        addAccountType("SE4401", "Skogskonto", AccountTypes.SAVINGS);
        addAccountType("SE4402", "Skogsskadekonto", AccountTypes.SAVINGS);
        addAccountType("SE4403", "Upphovsmannakonto", AccountTypes.SAVINGS);
        addAccountType("SE4404", "Allm Investeringskonto", AccountTypes.SAVINGS);
        addAccountType("SE4405", "Uppfinnarkonto", AccountTypes.SAVINGS);
        addAccountType("SE4500", "Sparkonto", AccountTypes.SAVINGS);
        addAccountType("SE4600", "Deposit", AccountTypes.SAVINGS);
        addAccountType("SE4601", "Deposit", AccountTypes.SAVINGS);
        addAccountType("SE4602", "Fastränteplacering", AccountTypes.SAVINGS);
        addAccountType("SE4603", "Dagsinlåning", AccountTypes.SAVINGS);
        addAccountType("SE4604", "Deposit", AccountTypes.SAVINGS);
        addAccountType("SE4605", "Fastränteplacering", AccountTypes.SAVINGS);
        addAccountType("SE4606", "Fastränteplacering", AccountTypes.SAVINGS);
        addAccountType("SE4607", "Fastränteplacering", AccountTypes.SAVINGS);
        addAccountType("SE4608", "Placering 4 År", AccountTypes.SAVINGS);
        addAccountType("SE4609", "Fastränteplacering", AccountTypes.SAVINGS);
        addAccountType("SE4610", "Bonuskonto, utgåva", AccountTypes.SAVINGS);

        addAccountType("SE4611", "Tillväxtkonto", AccountTypes.SAVINGS);
        addAccountType("SE0501", "ISK Trader likvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE4700", "Planeringskonto 2 År", AccountTypes.SAVINGS);
        addAccountType("SE4800", "Planeringskonto 3 År", AccountTypes.SAVINGS);
        addAccountType("SE4900", "Planeringskonto 4 År", AccountTypes.SAVINGS);
        addAccountType("SE5000", "Planeringskonto 5 År", AccountTypes.SAVINGS);
        addAccountType("SE5100", "Kapitalkonto", AccountTypes.SAVINGS);
        addAccountType("SE5200", "Skogslikvidkonto", AccountTypes.SAVINGS);
        addAccountType("SE5400", "Banksparkonto Ips", AccountTypes.SAVINGS);
        addAccountType("SE5500", "Ungbonus", AccountTypes.SAVINGS);
        addAccountType("SE5700", "Förmånskonto", AccountTypes.SAVINGS);
        addAccountType("SE5900", "Privatcertifikat", AccountTypes.SAVINGS);
        addAccountType("SE6000", "Affärsgiro", AccountTypes.SAVINGS);
        addAccountType("SE6013", "Zb Företagskonto Sub Acc", AccountTypes.SAVINGS);
        addAccountType("SE6100", "Plusgirokonto Ftg", AccountTypes.SAVINGS);
        addAccountType("SE6200", "Föreningsgiro", AccountTypes.SAVINGS);
        addAccountType("SE6300", "Pgkonto Förening", AccountTypes.SAVINGS);
        addAccountType("SE6600", "Plusgirokonto", AccountTypes.SAVINGS);
        addAccountType("SE6700", "Pensionssparkonto", AccountTypes.SAVINGS);
        addAccountType("SE7000", "Nostro", AccountTypes.SAVINGS);
        addAccountType("SE7100", "Loro 1", AccountTypes.SAVINGS);
        addAccountType("SE7200", "Kvk Valutatoppkonto", AccountTypes.SAVINGS);
        addAccountType("SE7300", "Kvk Toppkonto", AccountTypes.SAVINGS);
        addAccountType("SE7320", "Kvk Summeringskonto", AccountTypes.SAVINGS);
        addAccountType("SE7321", "Kvk Valutasumm Kto", AccountTypes.SAVINGS);
        addAccountType("SE7330", "Kvk Transaktionskto", AccountTypes.SAVINGS);
        addAccountType("SE7350", "Kvk Preliminäröppnat", AccountTypes.SAVINGS);
        addAccountType("SE7400", "Överföringskonto", AccountTypes.SAVINGS);
        addAccountType("SE7401", "Arbetskonto", AccountTypes.SAVINGS);
        addAccountType("SE7402", "Internkonto", AccountTypes.SAVINGS);
        addAccountType("SE7500", "Internt Kassakonto", AccountTypes.SAVINGS);
        addAccountType("SE7600", "Kontoöversikt", AccountTypes.SAVINGS);
        addAccountType("SE7601", "Zb Top Account", AccountTypes.SAVINGS);
        addAccountType("SE7602", "Zb Sub Group Level", AccountTypes.SAVINGS);
        addAccountType("SE7603", "Zb Sub Account", AccountTypes.SAVINGS);
        addAccountType("SE7604", "Zb Adjustement Account", AccountTypes.SAVINGS);
        addAccountType("SE7605", "Z Hdjustement Account", AccountTypes.SAVINGS);
        addAccountType("SE7700", "Centralkonto", AccountTypes.SAVINGS);
        addAccountType("SE7701", "Centralkonto", AccountTypes.SAVINGS);
        addAccountType("SE7702", "Centralkonto", AccountTypes.SAVINGS);
        addAccountType("SE7703", "Centralkonto", AccountTypes.SAVINGS);
        addAccountType("SE7800", "Samlingsnummer", AccountTypes.SAVINGS);
        addAccountType("SE7900", "Dispositionsnummer", AccountTypes.SAVINGS);
        addAccountType("SE7901", "Belastningsnummer", AccountTypes.SAVINGS);
        addAccountType("SE8001", "Business Gold", AccountTypes.CREDIT_CARD);
        addAccountType("SE8002", "First card", AccountTypes.CREDIT_CARD);


        addAccountType("SE46100601", "Bonuskonto 2006 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46100602", "Bonuskonto 2006 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46100603", "Bonuskonto 2006 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46100604", "Bonuskonto 2006 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46100605", "Bonuskonto 2006 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46100606", "Bonuskonto 2006 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46100607", "Bonuskonto 2006 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46100608", "Bonuskonto 2006 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46100609", "Bonuskonto 2006 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46100610", "Bonuskonto 2006 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46100611", "Bonuskonto 2006 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46100612", "Bonuskonto 2006 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46100701", "Bonuskonto 2007 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46100702", "Bonuskonto 2007 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46100703", "Bonuskonto 2007 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46100704", "Bonuskonto 2007 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46100705", "Bonuskonto 2007 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46100706", "Bonuskonto 2007 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46100707", "Bonuskonto 2007 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46100708", "Bonuskonto 2007 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46100709", "Bonuskonto 2007 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46100710", "Bonuskonto 2007 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46100711", "Bonuskonto 2007 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46100712", "Bonuskonto 2007 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46100719", "Bonuskonto 2007 utgåva 19", AccountTypes.CHECKING);
        addAccountType("SE46100801", "Bonuskonto 2008 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46100802", "Bonuskonto 2008 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46100803", "Bonuskonto 2008 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46100804", "Bonuskonto 2008 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46100805", "Bonuskonto 2008 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46100806", "Bonuskonto 2008 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46100807", "Bonuskonto 2008 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46100808", "Bonuskonto 2008 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46100809", "Bonuskonto 2008 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46100810", "Bonuskonto 2008 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46100811", "Bonuskonto 2008 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46100812", "Bonuskonto 2008 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46100901", "Bonuskonto 2009 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46100902", "Bonuskonto 2009 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46100903", "Bonuskonto 2009 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46100904", "Bonuskonto 2009 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46100905", "Bonuskonto 2009 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46100906", "Bonuskonto 2009 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46100907", "Bonuskonto 2009 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46100908", "Bonuskonto 2009 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46100909", "Bonuskonto 2009 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46100910", "Bonuskonto 2009 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46100911", "Bonuskonto 2009 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46100912", "Bonuskonto 2009 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46101001", "Bonuskonto 2010 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46101002", "Bonuskonto 2010 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46101003", "Bonuskonto 2010 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46101004", "Bonuskonto 2010 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46101005", "Bonuskonto 2010 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46101006", "Bonuskonto 2010 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46101007", "Bonuskonto 2010 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46101008", "Bonuskonto 2010 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46101009", "Bonuskonto 2010 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46101010", "Bonuskonto 2010 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46101011", "Bonuskonto 2010 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46101012", "Bonuskonto 2010 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46101101", "Bonuskonto 2011 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46101102", "Bonuskonto 2011 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46101103", "Bonuskonto 2011 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46101104", "Bonuskonto 2011 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46101105", "Bonuskonto 2011 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46101106", "Bonuskonto 2011 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46101107", "Bonuskonto 2011 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46101108", "Bonuskonto 2011 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46101109", "Bonuskonto 2011 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46101110", "Bonuskonto 2011 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46101111", "Bonuskonto 2011 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46101112", "Bonuskonto 2011 utgåva 12", AccountTypes.CHECKING);
        addAccountType("SE46101201", "Bonuskonto 2012 utgåva 1", AccountTypes.CHECKING);
        addAccountType("SE46101202", "Bonuskonto 2012 utgåva 2", AccountTypes.CHECKING);
        addAccountType("SE46101203", "Bonuskonto 2012 utgåva 3", AccountTypes.CHECKING);
        addAccountType("SE46101204", "Bonuskonto 2012 utgåva 4", AccountTypes.CHECKING);
        addAccountType("SE46101205", "Bonuskonto 2012 utgåva 5", AccountTypes.CHECKING);
        addAccountType("SE46101206", "Bonuskonto 2012 utgåva 6", AccountTypes.CHECKING);
        addAccountType("SE46101207", "Bonuskonto 2012 utgåva 7", AccountTypes.CHECKING);
        addAccountType("SE46101208", "Bonuskonto 2012 utgåva 8", AccountTypes.CHECKING);
        addAccountType("SE46101209", "Bonuskonto 2012 utgåva 9", AccountTypes.CHECKING);
        addAccountType("SE46101210", "Bonuskonto 2012 utgåva 10", AccountTypes.CHECKING);
        addAccountType("SE46101211", "Bonuskonto 2012 utgåva 11", AccountTypes.CHECKING);
        addAccountType("SE46101212", "Bonuskonto 2012 utgåva 12", AccountTypes.CHECKING);

        addAccountType("SE00018", "Personlån", AccountTypes.LOAN, Loan.Type.BLANCO);
        addAccountType("SE00019", "Privatlån", AccountTypes.LOAN, Loan.Type.BLANCO);
        addAccountType("SE00020", "Studentlån", AccountTypes.LOAN, Loan.Type.STUDENT);
        addAccountType("SE00021", "Startlån", AccountTypes.LOAN, Loan.Type.BLANCO);
        addAccountType("SE00022", "Privatlån", AccountTypes.LOAN, Loan.Type.BLANCO);
        addAccountType("SE00090", "Låna person, utan säkerhet", AccountTypes.LOAN, Loan.Type.BLANCO);
        addAccountType("SE00091", "Låna spar", AccountTypes.LOAN);
        addAccountType("SE00092", "Låna bostad", AccountTypes.LOAN, Loan.Type.MORTGAGE);
        addAccountType("SE00093", "Låna person, med säkerhet", AccountTypes.LOAN);
        addAccountType("SE00094", "Medlemslån", AccountTypes.LOAN, Loan.Type.MEMBERSHIP);
        addAccountType("SE00120", "Bolån", AccountTypes.LOAN, Loan.Type.MORTGAGE);
        addAccountType("SE00165", "Konvertibellån", AccountTypes.LOAN);
        addAccountType("SE00183", "Investeringslån", AccountTypes.LOAN);
        addAccountType("SE00200", "Bolån", AccountTypes.LOAN, Loan.Type.MORTGAGE);
        addAccountType("SE00210", "Företagslån Nordea Bank", AccountTypes.LOAN);
        addAccountType("SE00220", "Företagslån Hypotek", AccountTypes.LOAN);
        addAccountType("SE00301", "Vinstandelslån", AccountTypes.LOAN);
        addAccountType("SE00302", "Värdestegringslån", AccountTypes.LOAN);
        addAccountType("SE00303", "Konverteringslån", AccountTypes.LOAN);
        addAccountType("SE00304", "Optionslån", AccountTypes.LOAN);
        addAccountType("SE00305", "Villkorat tillskott", AccountTypes.LOAN);
        addAccountType("SE00409", "Byggnadskredit", AccountTypes.LOAN);
        addAccountType("SE00508", "Övrigt lån", AccountTypes.LOAN);
        addAccountType("SE00509", "Övrigt lån", AccountTypes.LOAN);
        addAccountType("SE00510", "Långt lån", AccountTypes.LOAN);
        addAccountType("SE00512", "Kommunlån", AccountTypes.LOAN);
        addAccountType("SE00532", "Räntekombinationslån", AccountTypes.LOAN);
        addAccountType("SE00544", "Flirt-lån", AccountTypes.LOAN);
        addAccountType("SE00590", "Miljögarantilån", AccountTypes.LOAN);
        addAccountType("SE00711", "Joku laina", AccountTypes.LOAN);
        addAccountType("SE00830", "Kort fasträntelån", AccountTypes.LOAN);
        addAccountType("SE00852", "Långt fasträntelån", AccountTypes.LOAN);
        addAccountType("SE00853", "Långt Investeringslån imm eller stibor", AccountTypes.LOAN);
        addAccountType("SE00913", "Kreditgaranti", AccountTypes.LOAN);
        addAccountType("SE00915", "Garanti", AccountTypes.LOAN);
        addAccountType("SE00919", "Garantifondförbindelse", AccountTypes.LOAN);
        addAccountType("SE10102", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10103", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10104", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10105", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10302", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10304", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10305", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10401", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10402", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10403", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE10404", "Bil- och Fritidskredit", AccountTypes.LOAN, Loan.Type.VEHICLE);
        addAccountType("SE30101", "Reverslån, Nordea finans", AccountTypes.LOAN);
        addAccountType("SE30103", "Reverslån, Nordea finans", AccountTypes.LOAN);
        addAccountType("SE30104", "Reverslån, Nordea finans", AccountTypes.LOAN);
        addAccountType("SE30301", "Reverslån, Nordea finans", AccountTypes.LOAN);

        addAccountType("FI0033","Time deposit invest. acct", AccountTypes.INVESTMENT);
        addAccountType("FI0035","Time deposit acc", AccountTypes.INVESTMENT);
        addAccountType("FI0313","Time deposit acc", AccountTypes.INVESTMENT);
        addAccountType("FI0314","ASP-tili", AccountTypes.INVESTMENT);
        addAccountType("FI0315","DepositPlus", AccountTypes.INVESTMENT);
        addAccountType("FI0316","InvestmentDeposit account", AccountTypes.INVESTMENT);
        addAccountType("FI0323","ProPersonnel Account", AccountTypes.INVESTMENT);
        addAccountType("FI0324", "Entrepreneurs PerkAccount", AccountTypes.INVESTMENT);
        addAccountType("FI0325","Continuous investment account", AccountTypes.INVESTMENT);
        addAccountType("FI0326","Junior account", AccountTypes.INVESTMENT);
        addAccountType("FI0328","Time deposit acc", AccountTypes.INVESTMENT);
        addAccountType("FI0329","Interest Extra Account", AccountTypes.INVESTMENT);
        addAccountType("FI033","Fixed-term investment account", AccountTypes.INVESTMENT);

        addAccountType("FI0331","Current account", AccountTypes.CHECKING);
        addAccountType("FI0337","CurrentAccount", AccountTypes.CHECKING);
        addAccountType("FI0339","Growth Account", AccountTypes.CHECKING);
        addAccountType("FI0340","Deposit account", AccountTypes.CHECKING);
        addAccountType("FI0342","Disposal account", AccountTypes.CHECKING);
        addAccountType("FI0343","ASP-tili", AccountTypes.CHECKING);
        addAccountType("FI0344","Servicing account", AccountTypes.CHECKING);
        addAccountType("FI0345","Tele account", AccountTypes.CHECKING);
        addAccountType("FI0346","Tele account", AccountTypes.CHECKING);
        addAccountType("FI0347","Parkki account", AccountTypes.CHECKING);
        addAccountType("FI0348","Disposal account", AccountTypes.CHECKING);
        addAccountType("FI0349","HomeFlex", AccountTypes.CHECKING);
        addAccountType("FI035","Fixed-term account", AccountTypes.CHECKING);
        addAccountType("FI0351","Direct usage account", AccountTypes.CHECKING);
        addAccountType("FI0352","Korkoplustili", AccountTypes.CHECKING);
        addAccountType("FI0353","PerkAccount", AccountTypes.CHECKING);
        addAccountType("FI0354","Direct usage credit", AccountTypes.CHECKING);
        addAccountType("FI0355","Time deposit sav. account", AccountTypes.CHECKING);
        addAccountType("FI0361","Fixed-term currency acc.", AccountTypes.CHECKING);
        addAccountType("FI0364","PS-tili", AccountTypes.CHECKING);
        addAccountType("FI0610","Sight curr. deposit acc", AccountTypes.CHECKING);
        addAccountType("FI0620","Personal currency acc", AccountTypes.CHECKING);
        addAccountType("FI0630","Currency account/Gold", AccountTypes.CHECKING);

        addAccountType("FI11111", "House loan", AccountTypes.LOAN);

        addAccountType("FI35300","Visa Silver", AccountTypes.CREDIT_CARD);
        addAccountType("FI35700","Visa Silver", AccountTypes.CREDIT_CARD);
        addAccountType("FI36300","Visa Gold", AccountTypes.CREDIT_CARD);
        addAccountType("FI36690","Nordea Electron", AccountTypes.CREDIT_CARD);
        addAccountType("FI39000","Nordea Credit", AccountTypes.CREDIT_CARD);
        addAccountType("FI39100","Nordea Credit", AccountTypes.CREDIT_CARD);
        addAccountType("FI39200","Nordea Gold", AccountTypes.CREDIT_CARD);
        addAccountType("FI39210","MasterCard Premium", AccountTypes.CREDIT_CARD);
        addAccountType("FI39300","Nordea Gold", AccountTypes.CREDIT_CARD);
        addAccountType("FI39310","MasterCard Premium", AccountTypes.CREDIT_CARD);
        addAccountType("FI39412","Finnair Plus MC", AccountTypes.CREDIT_CARD);
        addAccountType("FI39415","Stockmann MC", AccountTypes.CREDIT_CARD);
        addAccountType("FI39512","Finnair Plus MC", AccountTypes.CREDIT_CARD);
        addAccountType("FI39514","Tuohi MasterCard", AccountTypes.CREDIT_CARD);
        addAccountType("FI39515","Stockmann MC", AccountTypes.CREDIT_CARD);
        addAccountType("FI39700","Nordea Platinum", AccountTypes.CREDIT_CARD);
        addAccountType("FI82020","MasterCard Liiga", AccountTypes.CREDIT_CARD);
        addAccountType("FI82120","MasterCard Liiga", AccountTypes.CREDIT_CARD);
        addAccountType("FI82400","Visa Debit", AccountTypes.CREDIT_CARD);
        addAccountType("FI82500","Business Visa Debit", AccountTypes.CREDIT_CARD);
        addAccountType("FI82890","Nordea Electron", AccountTypes.CREDIT_CARD);
        addAccountType("FI82900","Nordea Credit", AccountTypes.CREDIT_CARD);
        addAccountType("FI83000","Nordea Credit", AccountTypes.CREDIT_CARD);
        addAccountType("FI83100","Nordea Gold", AccountTypes.CREDIT_CARD);
        addAccountType("FI83200","Nordea Black", AccountTypes.CREDIT_CARD);
        addAccountType("FI0", "A1 Car Credit", AccountTypes.CREDIT_CARD);
    }

    private static void addAccountType(String code, String name, AccountTypes type) {
        ACCOUNT_TYPES_BY_CODE.put(code, type);
        ACCOUNT_TYPES_BY_NAME.put(name, type);
        ACCOUNT_NAMES_BY_CODE.put(code, name);
    }

    private static void addAccountType(String code, String name, AccountTypes type, Loan.Type loanType) {
        addAccountType(code, name, type);
        LOAN_TYPES_BY_CODE.put(code, loanType);
    }

    public static String getAccountNameForCode(String code) {
        return ACCOUNT_NAMES_BY_CODE.get(code);
    }

    public static AccountTypes getAccountTypeForCode(String code) {
        return ACCOUNT_TYPES_BY_CODE.get(code);
    }

    public static AccountTypes getAccountTypeForName(String name) {
        return ACCOUNT_TYPES_BY_NAME.get(name);
    }


    public static Loan.Type getLoanTypeForCode(String productTypeExtension) {
        if (Strings.isNullOrEmpty(productTypeExtension) || !LOAN_TYPES_BY_CODE.containsKey(productTypeExtension)) {
            return Loan.Type.OTHER;
        }

        return LOAN_TYPES_BY_CODE.get(productTypeExtension);
    }

    public static void parseTransactionDescription(String description, Transaction transaction) {
        if (description.startsWith("Reservation ")) {
            description = description.substring(12);
            transaction.setPending(true);
        } else if (Objects.equal(description, "Reservation")) {
            description = "Reservation";
            transaction.setPending(true);
        }

        if (description.startsWith("Kortköp ")) {
            description = description.substring(8);
            transaction.setType(TransactionTypes.CREDIT_CARD);

            if (!transaction.isPending()) {
                try {
                    transaction.setDate(DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT
                            .parse(description.substring(0, 6))));
                    description = description.substring(7);
                } catch (Exception e) {
                    // NOOP: The date is not present.
                }
            }
        } else if (description.startsWith("Betalning ")) {
            description = description.substring(10);

            int descriptionIndex = 0;

            if (description.startsWith("PG") || description.startsWith("BG")) {
                descriptionIndex = description.indexOf(" ", 4);

                transaction.setPayload(TransactionPayloadTypes.GIRO, description.substring(0, descriptionIndex));
            }

            description = description.substring(descriptionIndex);
            transaction.setType(TransactionTypes.PAYMENT);
        } else if (description.startsWith("Överföring ")) {
            description = description.substring(11);

            transaction.setType(TransactionTypes.TRANSFER);
            transaction.setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL, description);
        } else if (description.startsWith("Kontantuttag ")) {
            description = description.substring(13);

            if (description.startsWith("utl/aut ")) {
                description = description.substring(8);
            }

            try {
                transaction.setDate(DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT
                        .parse(description.substring(0, 6))));
                description = description.substring(7);
            } catch (Exception e) {
                // NOOP: The date is not present.
            }

            transaction.setType(TransactionTypes.WITHDRAWAL);
        } else if (description.startsWith("Autogiro ")) {
            description = description.substring(9);

            transaction.setType(TransactionTypes.PAYMENT);
            transaction.setPayload(TransactionPayloadTypes.GIRO, "AG");
        } else if (description.startsWith("Köp ")) {
            description = description.substring(4);

            transaction.setType(TransactionTypes.PAYMENT);
        } else if (description.startsWith("Återköp ")) {
            description = description.substring(8);

            try {
                transaction.setDate(DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_INTEGER_DATE_COMPACT
                        .parse(description.substring(0, 6))));
                description = description.substring(7);
            } catch (Exception e) {
                // NOOP: The date is not present.
            }

            transaction.setPayload(TransactionPayloadTypes.CHARGEBACK_OR_RETURN, "true");
            transaction.setType(TransactionTypes.CREDIT_CARD);
        }

        transaction.setDescription(CLEANUP_JOINER.join(CLEANUP_SPLITTER.split(description)));
    }

    public static void parseTransactionTypeForFI(String transactionType, Transaction transaction) {
        if (!Strings.isNullOrEmpty(transactionType)) {

            switch (transactionType) {
                case "e-lasku":     // translate( E-faktura )
                    transaction.setType(TransactionTypes.PAYMENT);
                    break;
                case "e-maksu":     // translate( E-betalning )
                    transaction.setType(TransactionTypes.PAYMENT);
                    break;
            case "Pano": // transalate( Insättning )
                    transaction.setType(TransactionTypes.TRANSFER);
                    break;
            case "Korttiosto": // translate( Kortköp )
                    transaction.setType(TransactionTypes.CREDIT_CARD);
                    break;
            case "Oma siirto": // translate( Min överföring )
                    transaction.setType(TransactionTypes.TRANSFER);
                    break;
            case "Käteispano": // translate( Kontantbetalningar )
                    transaction.setType(TransactionTypes.WITHDRAWAL);
                    break;
            }
        }
    }

    /**
     * Helper method to mask an account number (in order to make it compatible with the web agent).
     */
    public static String maskAccountNumber(String accountNumber) {
        return "************" + accountNumber.substring(accountNumber.length() - 4);
    }

    /**
     * Code from Nordea iOS app v. 2.2.1
     *
     *  Did not understand mappings for values in app:
     *  "SHYB"; Should probably be Stadshypotek Bank (part of Handelsbanken)
     *  "OREF";
     *  "OREG";
     */
    public static String lookupBeneficiaryBankId(AccountIdentifier accountIdentifier) {
        SwedishIdentifier swedishIdentifier = accountIdentifier.to(SwedishIdentifier.class);
        Optional<Details> bankDetails = ClearingNumber.get(swedishIdentifier.getClearingNumber());

        if (!bankDetails.isPresent()) {
            return null;
        }

        Optional<String> bankId = BeneficiaryBankId.getId(swedishIdentifier);
        return bankId.orElse(null);
    }

    public static Optional<Bank> lookupBankFromBeneficiaryId(String beneficiaryBankId) {
        if (Strings.isNullOrEmpty(beneficiaryBankId)) {
            return Optional.empty();
        }

        return BeneficiaryBankId.getBank(beneficiaryBankId);
    }

    public static Predicate<PaymentEntity> getPaymentFilter(final List<ProductEntity> accounts) {
        return paymentEntity -> {
            if (paymentEntity.getFromAccountId() == null) {
                return false;
            }

            for (ProductEntity account : accounts) {
                if (Objects.equal(account.getAccountId(), paymentEntity.getFromAccountId())) {
                    return true;
                }
            }

            return false;
        };
    }

    public static Predicate<ProductEntity> getAccountIdFilter(final Set<String> accountIds) {
        return productEntity -> productEntity != null && accountIds.contains(productEntity.getAccountId());
    }

    public static Predicate<? super ProductEntity> getProductsOfType(final String... types) {
        final Set<String> setOfTypes = Sets.newHashSet(types);

        return (Predicate<ProductEntity>) pe -> {
            if (pe == null) {
                return false;
            }

            return setOfTypes.contains(pe.getNordeaProductType());
        };
    }

    private static final class BeneficiaryBankId {
        private static final String DANSKE_OGB = "OGB";
        private static final String DANSKE_DDB = "DDB";

        private static final ImmutableMap<Bank, String> beneficiaryBankIds = ImmutableMap
                .<Bank, String>builder()
                .put(Bank.NORDEA, "NB")
                .put(Bank.NORDEA_PERSONKONTO, "NB")
                .put(Bank.SWEDBANK, "FSPA")
                .put(Bank.HANDELSBANKEN, "SHB")
                .put(Bank.SEB, "SEB")
                .put(Bank.AVANZA_BANK, "AVANZ")
                .put(Bank.CITIBANK, "CITI")
                .put(Bank.DEN_NORSKE_BANK_SVERIGE, "DNBSE")
                .put(Bank.DEN_NORSKE_BANK, "DNBSE")
                .put(Bank.ERIK_PENSER_BANKAKTIEBOLAG, "EPENS")
                .put(Bank.FOREX_BANK, "FOREX")
                .put(Bank.SANTANDER_CONSUMER_BANK, "SANTA")
                .put(Bank.ICA_BANKEN, "ICA")
                .put(Bank.IKANO_BANK, "IKANO")
                .put(Bank.LANDSHYPOTEK, "LAHYP")
                .put(Bank.LANSFORSAKRINGAR_BANK, "LFB")
                .put(Bank.MARGINALEN_BANK, "SALAB")
                .put(Bank.NORDNET_BANK, "NNSE")
                .put(Bank.PLUSGIROT_BANK, "PGB")
                .put(Bank.PLUSGIROT, "PGB")
                .put(Bank.JAKBANKEN, "JAK")
                .put(Bank.EKOBANKEN, "EB")
                .put(Bank.RESURS_BANK, "RB")
                .put(Bank.RIKSGALDEN, "RGK")
                .put(Bank.ROYAL_BANK_OF_SCOTLAND, "RBS")
                .put(Bank.SBAB, "SBAB")
                .put(Bank.SKANDIABANKEN, "SKB")
                .put(Bank.SPARBANKEN_SYD, "SYD")
                .put(Bank.ALANDSBANKEN, "ALAB")
                .put(Bank.AMFA_BANK, "AMFA")
                .put(Bank.BLUESTEP_FINANS, "BSTP")
                .put(Bank.BNP_PARIBAS_FORTIS, "BNPPF")
                .put(Bank.LAN_O_SPAR_BANK, "LSBSE")
                .put(Bank.NORDAX_BANK, "NDX")
                .build();

        private static final ImmutableMap<String, Bank> oldBeneficiaryBankIdsToBank = ImmutableMap
                .<String, Bank>builder()
                .put("ORES", Bank.SWEDBANK)
                .put("OREF", Bank.SWEDBANK)
                .put("OREG", Bank.SWEDBANK)
                .build();

        public static Optional<String> getId(SwedishIdentifier swedishIdentifier) {
            final Bank bank = swedishIdentifier.getBank();

            switch (bank) {
            case DANSKE_BANK:
            case DANSKE_BANK_SVERIGE:
                // According to Nordea, account numbers for danske with length 11 belongs to Östgöta Bank, others to DDB
                if (Objects.equal(swedishIdentifier.getIdentifier(new DefaultAccountIdentifierFormatter()).length(), 11)) {
                    return Optional.of(DANSKE_OGB);
                } else {
                    return Optional.of(DANSKE_DDB);
                }
            default:
                return Optional.ofNullable(beneficiaryBankIds.get(bank));
            }
        }

        public static Optional<Bank> getBank(final String beneficiaryBankId) {
            switch (beneficiaryBankId) {
            case DANSKE_OGB:
            case DANSKE_DDB: // Don't know if there is a correct match between OGB/DDB and DANSKE_BANK/DANSKE_BANK_SVERIGE so using same for both here
                return Optional.of(Bank.DANSKE_BANK);
            default:
                Optional<Map.Entry<Bank, String>> matchingBeneficiaryBankId = beneficiaryBankIds.entrySet()
                        .stream().filter(b -> matchesId(beneficiaryBankId).apply(b)).findFirst();

                return matchingBeneficiaryBankId.isPresent() ?
                        Optional.of(matchingBeneficiaryBankId.get().getKey()) : Optional.ofNullable(oldBeneficiaryBankIdsToBank.get(beneficiaryBankId));
            }
        }

        private static Predicate<Map.Entry<Bank, String>> matchesId(final String beneficiaryBankId) {
            return bankStringEntry -> Objects.equal(beneficiaryBankId, bankStringEntry.getValue());
        }
    }
}
