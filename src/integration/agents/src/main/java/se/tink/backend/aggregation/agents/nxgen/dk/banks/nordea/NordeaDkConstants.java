package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.UrlEnum;
import se.tink.backend.agents.rpc.AccountTypes;

public class NordeaDkConstants {
    public static final String MARKET_CODE = "DK";
    public static final String CURRENCY = "DKK";

    public static class AccountType {
        private static final Map<String, String> ACCOUNT_NAMES_BY_CODE = Maps.newHashMap();
        private static final Map<String, AccountTypes> ACCOUNT_TYPES_BY_CODE = Maps.newHashMap();
        private static final Map<String, LoanDetails.Type> LOAN_TYPES_BY_CODE = Maps.newHashMap();

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
            addAccountType("SE0501", "ISK Trader likvidkonto", AccountTypes.CHECKING);
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

            addAccountType("SE00018", "Personlån", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("SE00019", "Privatlån", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("SE00020", "Studentlån", AccountTypes.LOAN, LoanDetails.Type.STUDENT);
            addAccountType("SE00021", "Startlån", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("SE00022", "Privatlån", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("SE00090", "Låna person, utan säkerhet", AccountTypes.LOAN, LoanDetails.Type.BLANCO);
            addAccountType("SE00091", "Låna spar", AccountTypes.LOAN);
            addAccountType("SE00092", "Låna bostad", AccountTypes.LOAN, LoanDetails.Type.MORTGAGE);
            addAccountType("SE00093", "Låna person, med säkerhet", AccountTypes.LOAN);
            addAccountType("SE00094", "Medlemslån", AccountTypes.LOAN, LoanDetails.Type.MEMBERSHIP);
            addAccountType("SE00120", "Bolån", AccountTypes.LOAN, LoanDetails.Type.MORTGAGE);
            addAccountType("SE00165", "Konvertibellån", AccountTypes.LOAN);
            addAccountType("SE00183", "Investeringslån", AccountTypes.LOAN);
            addAccountType("SE00200", "Bolån", AccountTypes.LOAN, LoanDetails.Type.MORTGAGE);
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
            addAccountType("SE10102", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10103", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10104", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10105", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10302", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10304", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10305", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10401", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10402", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10403", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE10404", "Bil- och Fritidskredit", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("SE30101", "Reverslån, Nordea finans", AccountTypes.LOAN);
            addAccountType("SE30103", "Reverslån, Nordea finans", AccountTypes.LOAN);
            addAccountType("SE30104", "Reverslån, Nordea finans", AccountTypes.LOAN);
            addAccountType("SE30301", "Reverslån, Nordea finans", AccountTypes.LOAN);
            addAccountType("DK0055", "Grundkonto med kredit", AccountTypes.CHECKING);
            addAccountType("DK0057", "Boligopsparing", AccountTypes.SAVINGS);
            addAccountType("DK0058", "Uddannelsesopsparing", AccountTypes.SAVINGS);
            addAccountType("DK0076", "Medarbejderkonto", AccountTypes.CHECKING);
            addAccountType("DK0153", "Pluskonto", AccountTypes.SAVINGS);
            addAccountType("DK0209", "Pluskonto", AccountTypes.SAVINGS);
            addAccountType("DK0210", "Pluskonto", AccountTypes.SAVINGS);
            addAccountType("DK0255", "Særlig aftale-indlån", AccountTypes.SAVINGS);
            addAccountType("DK0258", "Gevinstopsparing", AccountTypes.SAVINGS);
            addAccountType("DK0267", "Juniorkonto", AccountTypes.CHECKING);
            addAccountType("DK0269", "Ungdomskonto", AccountTypes.CHECKING);
            addAccountType("DK0271", "Grundkonto med betaling", AccountTypes.CHECKING);
            addAccountType("DK0274", "Grundkonto", AccountTypes.CHECKING);
            addAccountType("DK0290", "Særlig grundkonto kredit", AccountTypes.CHECKING);
            addAccountType("DK0294", "Grundkonto med kredit", AccountTypes.CHECKING);
            addAccountType("DK0300", "Grundkonto med betaling", AccountTypes.CHECKING);
            addAccountType("DK0301", "Grundkonto med betaling", AccountTypes.CHECKING);
            addAccountType("DK0302", "Ungdomskonto", AccountTypes.CHECKING);
            addAccountType("DK0304", "Check-in med kredit", AccountTypes.CHECKING);
            addAccountType("DK0308", "Check-in konto", AccountTypes.CHECKING);
            addAccountType("DK0317", "Check-in konto med bet.", AccountTypes.CHECKING);
            addAccountType("DK0323", "Kredit", AccountTypes.LOAN); // credit card?
            addAccountType("DK0332", "Kredit", AccountTypes.LOAN);
            addAccountType("DK0335", "Forretningskonto uden kredit", AccountTypes.CHECKING);
            addAccountType("DK0339", "Forretningskonto med kredit", AccountTypes.CHECKING);
            addAccountType("DK0348", "Depotplejekonto", AccountTypes.INVESTMENT);
            addAccountType("DK0371", "Opsparingskonto", AccountTypes.SAVINGS);
            addAccountType("DK0372", "Opsparingskonto", AccountTypes.SAVINGS);
            addAccountType("DK0373", "Medarbejderkonto", AccountTypes.CHECKING);
            addAccountType("DK0460", "Grundkonto-familie", AccountTypes.CHECKING);
            addAccountType("DK0463", "Særlig forretningskonto", AccountTypes.CHECKING);
            addAccountType("DK0470", "Grundkonto Nordea Prioritet", AccountTypes.CHECKING);
            addAccountType("DK0480", "Grundkonto Andelsprioritet", AccountTypes.CHECKING);
            addAccountType("DK0485", "Grundkonto Bol.køb", AccountTypes.LOAN);
            addAccountType("DK0486", "Check-in kredit 29-31", AccountTypes.CHECKING);
            addAccountType("DK0488", "Check-in betaling 29-31 år", AccountTypes.CHECKING);
            addAccountType("DK0490", "Private Banking konto", AccountTypes.INVESTMENT);
            addAccountType("DK0492", "Investeringskredit, privat", AccountTypes.LOAN);
            addAccountType("DK0493", "Check-in opsparing", AccountTypes.CHECKING);
            addAccountType("DK0499", "Grundkonto med betaling", AccountTypes.CHECKING);
            addAccountType("DK0501", "Check-in konto med bet.", AccountTypes.CHECKING);
            addAccountType("DK0507", "Grundkonto Prioritet, SDO", AccountTypes.CHECKING);
            addAccountType("DK0509", "Pluskonto Fri", AccountTypes.SAVINGS);
            addAccountType("DK0516", "Aktieopsparing", AccountTypes.INVESTMENT);
            addAccountType("DK0532", "Grundkonto Prioritet, Andel", AccountTypes.CHECKING);
            addAccountType("DK0536", "Virksomhedskonto", AccountTypes.CHECKING);
            addAccountType("DK0537", "Virksomhedskonto", AccountTypes.CHECKING);
            addAccountType("DK0539", "Lønkonto med kredit", AccountTypes.CHECKING);
            addAccountType("DK0540", "Advice+ opsparing", AccountTypes.SAVINGS);
            addAccountType("DK0547", "Grundkonto Nordea Prioritet", AccountTypes.CHECKING);
            addAccountType("DK0549", "Grundkonto Bol.køb", AccountTypes.LOAN);
            addAccountType("DK0551", "Grundkonto Prioritet, SDO", AccountTypes.CHECKING);
            addAccountType("DK0553", "Bilkøb", AccountTypes.LOAN, LoanDetails.Type.VEHICLE);
            addAccountType("DK0558", "Juniorkonto", AccountTypes.CHECKING);
            addAccountType("DK0560", "Check-in opsparing 29-31 år", AccountTypes.CHECKING);
        }

        public static String getAccountNameForCode(String code) {
            return ACCOUNT_NAMES_BY_CODE.getOrDefault(code, "");
        }

        public static AccountTypes getAccountTypeForCode(String code) {
            return ACCOUNT_TYPES_BY_CODE.getOrDefault(code, AccountTypes.CHECKING);
        }

        public static LoanDetails.Type getLoanTypeForCode(String productTypeExtension) {
            return LOAN_TYPES_BY_CODE.getOrDefault(productTypeExtension, LoanDetails.Type.OTHER);
        }

        private static void addAccountType(String code, String name, AccountTypes type) {
            ACCOUNT_TYPES_BY_CODE.put(code, type);
            ACCOUNT_NAMES_BY_CODE.put(code, name);
        }

        private static void addAccountType(String code, String name, AccountTypes type, LoanDetails.Type loanType) {
            addAccountType(code, name, type);
            LOAN_TYPES_BY_CODE.put(code, loanType);
        }
    }

    public enum Url implements UrlEnum {
        NEMID_LOGIN(NordeaV20Constants.Url.getAuthenticationEndpoint("/SecurityToken")),
        AGREEMENT_AUTHORIZATION(NordeaV20Constants.Url.getAuthenticationEndpoint("/SecurityToken")),
        INITIAL_PARAMETERS(NordeaV20Constants.Url.getAuthenticationEndpoint("/InitialParameters"));

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
    }

    public static class Authentication {
        public static final String DEFAULT_AUTH_LEVEL = "1";
        public static final String LOGIN_TYPE_NEMID = "MNEMID-LOGON";
    }
}
