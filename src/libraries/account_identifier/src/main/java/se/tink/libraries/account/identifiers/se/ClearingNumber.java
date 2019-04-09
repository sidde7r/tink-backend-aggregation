package se.tink.libraries.account.identifiers.se;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClearingNumber {
    private static RangeMap<Integer, Details> map;

    private static final Pattern PATTERN_CLEARING = Pattern.compile("8?[0-9]{4}");

    static {
        map = TreeRangeMap.create();

        /*
        These are shamelessly ripped (02/10/2015) from:
        https://github.com/barsoom/banktools-se/blob/master/lib/banktools-se/account/clearing_number.rb
         */

        // Get display name from Provider DB ?
        // Add providername (e.g. handelsbanken-bankid) ?

        register(1000, 1099, new Details(Bank.SVERIGES_RIKSBANK));
        register(1100, 1199, new Details(Bank.NORDEA));
        register(1200, 1399, new Details(Bank.DANSKE_BANK));
        register(1400, 2099, new Details(Bank.NORDEA));
        register(2300, 2399, new Details(Bank.ALANDSBANKEN));
        register(2400, 2499, new Details(Bank.DANSKE_BANK));
        register(3000, 3299, new Details(Bank.NORDEA));
        register(3300, 3300, new Details(Bank.NORDEA_PERSONKONTO));
        register(3301, 3399, new Details(Bank.NORDEA));
        register(3400, 3409, new Details(Bank.LANSFORSAKRINGAR_BANK));
        register(3410, 3781, new Details(Bank.NORDEA));
        register(3782, 3782, new Details(Bank.NORDEA));
        register(3783, 4999, new Details(Bank.NORDEA));
        register(5000, 5999, new Details(Bank.SEB));
        register(6000, 6999, new Details(Bank.HANDELSBANKEN));
        register(7000, 7999, new Details(Bank.SWEDBANK));
        register(8000, 8999, new Details(Bank.SWEDBANK).setClearingNumberLength(5));
        register(9020, 9029, new Details(Bank.LANSFORSAKRINGAR_BANK));
        register(9040, 9049, new Details(Bank.CITIBANK));
        register(9050, 9059, new Details(Bank.HSB_BANK));
        register(9060, 9069, new Details(Bank.LANSFORSAKRINGAR_BANK));
        register(9090, 9099, new Details(Bank.ROYAL_BANK_OF_SCOTLAND));
        register(9100, 9109, new Details(Bank.NORDNET_BANK));
        register(9120, 9124, new Details(Bank.SEB));
        register(9130, 9149, new Details(Bank.SEB));
        register(9150, 9169, new Details(Bank.SKANDIABANKEN));
        register(9170, 9179, new Details(Bank.IKANO_BANK));
        register(9180, 9189, new Details(Bank.DANSKE_BANK_SVERIGE));
        register(9190, 9199, new Details(Bank.DEN_NORSKE_BANK_SVERIGE));
        register(9230, 9239, new Details(Bank.MARGINALEN_BANK));
        register(9250, 9259, new Details(Bank.SBAB));
        register(9260, 9269, new Details(Bank.DEN_NORSKE_BANK));
        register(9270, 9279, new Details(Bank.ICA_BANKEN));
        register(9280, 9289, new Details(Bank.RESURS_BANK));
        register(9300, 9349, new Details(Bank.SPARBANKEN_ORESUND));
        register(9390, 9399, new Details(Bank.LANDSHYPOTEK));
        register(9400, 9449, new Details(Bank.FOREX_BANK));
        register(9450, 9459, new Details(Bank.SAMPO_BANK));
        register(9460, 9469, new Details(Bank.SANTANDER_CONSUMER_BANK));
        register(9470, 9479, new Details(Bank.FORTIS_BANK));
        register(9500, 9547, new Details(Bank.PLUSGIROT));
        register(9548, 9548, new Details(Bank.EKOBANKEN));
        register(9549, 9549, new Details(Bank.JAKBANKEN));
        register(9550, 9569, new Details(Bank.AVANZA_BANK));
        register(9570, 9579, new Details(Bank.SPARBANKEN_SYD));
        register(9590, 9599, new Details(Bank.ERIK_PENSER_BANKAKTIEBOLAG));
        register(9630, 9639, new Details(Bank.LAN_O_SPAR_BANK));
        register(9640, 9649, new Details(Bank.NORDAX_BANK));
        register(9660, 9669, new Details(Bank.AMFA_BANK));
        register(9680, 9689, new Details(Bank.BLUESTEP_FINANS));
        register(9880, 9899, new Details(Bank.RIKSGALDEN));
        register(9960, 9969, new Details(Bank.PLUSGIROT_BANK));

        // Added from us. If there were to be any real clearingnumber on 9999 we need to move this.
        register(9999, 9999, new Details(Bank.DEMO_BANK));
    }

    public static class Details {
        private final Bank bank;
        private int clearingNumberLength = 4;

        public Details(Bank bank) {
            this.bank = bank;
        }

        Details setClearingNumberLength(int length) {
            clearingNumberLength = length;
            return this;
        }

        public Bank getBank() {
            return bank;
        }

        public String getBankName() {
            return bank.getDisplayName();
        }

        public int getClearingNumberLength() {
            return clearingNumberLength;
        }
    }

    public enum Bank {
        DEMO_BANK("Demo Bank"),

        ALANDSBANKEN("Ålandsbanken"),
        AMFA_BANK("Amfa Bank"),
        AVANZA_BANK("Avanza Bank"),
        BLUESTEP_FINANS("Bluestep Finans"),
        BNP_PARIBAS_FORTIS("BNP Paribas Fortis"),
        CITIBANK("Citibank"),
        DANSKE_BANK("Danske Bank"),
        DANSKE_BANK_SVERIGE("Danske Bank"),
        DEN_NORSKE_BANK("Den Norske Bank"),
        DEN_NORSKE_BANK_SVERIGE("Den Norske Bank"),
        EKOBANKEN("EkoBanken"),
        ERIK_PENSER_BANKAKTIEBOLAG("Erik Penser Bankaktiebolag"),
        FOREX_BANK("Forex Bank"),
        FORTIS_BANK("Fortis Bank"),
        HANDELSBANKEN("Handelsbanken"),
        ICA_BANKEN("ICA Banken"),
        IKANO_BANK("Ikano Bank"),
        JAKBANKEN("JakBanken"),
        LANDSHYPOTEK("Landshypotek"),
        LANSFORSAKRINGAR_BANK("Länsförsäkringar Bank"),
        LAN_O_SPAR_BANK("Lån & Spar Bank"),
        MARGINALEN_BANK("Marginalen Bank"),
        NORDAX_BANK("Nordax Bank"),
        NORDEA("Nordea"),
        NORDEA_PERSONKONTO("Nordea"),
        NORDNET_BANK("Nordnet Bank"),
        PLUSGIROT("Plusgirot"), // Nordea/Plusgirot
        PLUSGIROT_BANK("Plusgirot Bank"), // Nordea/Plusgirot
        RESURS_BANK("Resurs Bank"),
        RIKSGALDEN("Riksgälden"),
        ROYAL_BANK_OF_SCOTLAND("Royal Bank of Scotland"),
        SAMPO_BANK("Sampo Bank"),
        SANTANDER_CONSUMER_BANK("Santander Consumer Bank"),
        SBAB("SBAB"),
        SEB("SEB"),
        SKANDIABANKEN("Skandiabanken"),
        SPARBANKEN_ORESUND("Sparbanken Öresund"),
        SPARBANKEN_SYD("Sparbanken Syd"),
        SVERIGES_RIKSBANK("Sveriges Riksbank"),
        SWEDBANK("Swedbank"),
        HSB_BANK("HSB Bank");

        final String bankName;

        Bank(String bankName) {
            this.bankName = bankName;
        }

        public String getDisplayName() {
            return bankName;
        }
    }

    private static void register(Integer min, Integer max, Details value) {
        map.put(Range.closed(min, max), value);
    }

    private static Details get(Integer clearingNumber) {

        if (Range.closed(80000, 89999).contains(clearingNumber)) {
            // only take first four digits into consideration
            clearingNumber = clearingNumber / 10;
        } else if (clearingNumber > 9999) {
            return null;
        }

        return map.get(clearingNumber);
    }

    public static Optional<Details> get(String clearingNumber) {
        if (Strings.isNullOrEmpty(clearingNumber)) {
            return Optional.empty();
        }

        try {
            int clearingNumberValue = Integer.parseInt(clearingNumber);
            return Optional.ofNullable(get(clearingNumberValue));
        } catch (NumberFormatException invalidNumberFormatException) {
            return Optional.empty();
        }
    }

    public static boolean isValidClearing(String clearing) {
        if (Strings.isNullOrEmpty(clearing)) {
            return false;
        }

        Matcher matcher = PATTERN_CLEARING.matcher(clearing);

        return matcher.matches();
    }
}
