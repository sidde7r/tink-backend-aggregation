package se.tink.backend.aggregation.agents.banks.danskebank;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.system.rpc.Portfolio;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.date.DateUtils;

public class DanskeUtils {
    private static final ImmutableMap<ClearingNumber.Bank, String> BANK_ID_LOOKUP_TABLE = ImmutableMap.<ClearingNumber.Bank, String>builder()
            .put(ClearingNumber.Bank.AMFA_BANK, "9660AMFA BANK")
            .put(ClearingNumber.Bank.AVANZA_BANK, "9550AVANZA BANK")
            .put(ClearingNumber.Bank.BLUESTEP_FINANS, "9680BLUESTEP FINANS")
            .put(ClearingNumber.Bank.CITIBANK, "9040CITIBANK")
            .put(ClearingNumber.Bank.DANSKE_BANK_SVERIGE, "1200DANSKE BANK SVERIGE")
            .put(ClearingNumber.Bank.DANSKE_BANK, "1200DANSKE BANK SVERIGE")
            .put(ClearingNumber.Bank.DEN_NORSKE_BANK_SVERIGE, "9190DNB BANK")
            .put(ClearingNumber.Bank.ERIK_PENSER_BANKAKTIEBOLAG, "9590ERIK PENSER BANKAKTIEBOLAG")
            .put(ClearingNumber.Bank.FOREX_BANK, "9400FOREX BANK AB")
            .put(ClearingNumber.Bank.FORTIS_BANK, "9470BNP Paribas Fortis")
            .put(ClearingNumber.Bank.HANDELSBANKEN, "6000HANDELSBANKEN")
            .put(ClearingNumber.Bank.ICA_BANKEN, "9270ICA BANKEN")
            .put(ClearingNumber.Bank.IKANO_BANK, "9170IKANO BANK")
            .put(ClearingNumber.Bank.LANDSHYPOTEK, "9390LANDSHYPOTEK AB")
            .put(ClearingNumber.Bank.LANSFORSAKRINGAR_BANK, "9020LÄNSFÖRSÄKRINGAR BANK AB")
            .put(ClearingNumber.Bank.LAN_O_SPAR_BANK, "9630Lån og Spar Bank Sverige")
            .put(ClearingNumber.Bank.MARGINALEN_BANK, "9230MARGINALEN BANK")
            .put(ClearingNumber.Bank.NORDAX_BANK, "9640NORDAX BANK AB")
            .put(ClearingNumber.Bank.NORDEA, "3000NORDEA")
            .put(ClearingNumber.Bank.NORDEA_PERSONKONTO, "3333NORDEA - PERSONKONTON")
            .put(ClearingNumber.Bank.PLUSGIROT, "9500NORDEA - PLUSGIROT")
            .put(ClearingNumber.Bank.NORDNET_BANK, "9100NORDNET BANK")
            .put(ClearingNumber.Bank.RESURS_BANK, "9280RESURS BANK")
            .put(ClearingNumber.Bank.RIKSGALDEN, "9880RIKSGÄLDEN")
            .put(ClearingNumber.Bank.ROYAL_BANK_OF_SCOTLAND, "9090ROYAL BANK OF SCOTLAND")
            .put(ClearingNumber.Bank.SANTANDER_CONSUMER_BANK, "9460Santander Consumer Bank AS")
            .put(ClearingNumber.Bank.SBAB, "9250SBAB BANK")
            .put(ClearingNumber.Bank.SEB, "5000SEB")
            .put(ClearingNumber.Bank.SKANDIABANKEN, "9150SKANDIABANKEN")
            .put(ClearingNumber.Bank.SPARBANKEN_ORESUND, "8000SPARBANKEN ÖRESUND")
            .put(ClearingNumber.Bank.SPARBANKEN_SYD, "9570SPARBANKEN SYD")
            .put(ClearingNumber.Bank.SVERIGES_RIKSBANK, "1000SVERIGES RIKSBANK")
            .put(ClearingNumber.Bank.SWEDBANK, "8000SWEDBANK")
            .put(ClearingNumber.Bank.ALANDSBANKEN, "2300ÅLANDSBANKEN")
            .build();

    public static final TypeMapper<Portfolio.Type> PORTFOLIO_TYPE_MAPPER = TypeMapper.<Portfolio.Type>builder()
            .put(Portfolio.Type.DEPOT, "Depå")
            .put(Portfolio.Type.ISK, "Investeringssparkonto")
            .build();

    public static String getBankId(AccountIdentifier accountIdentifier) {
        if (!accountIdentifier.is(AccountIdentifier.Type.SE)) {
            throw new IllegalStateException("Only Swedish accounts are supported");
        }

        SwedishIdentifier swedishIdentifier = accountIdentifier.to(SwedishIdentifier.class);

        Optional<ClearingNumber.Details> clearing = ClearingNumber.get(swedishIdentifier.getClearingNumber());

        if (!clearing.isPresent()) {
            throw new IllegalStateException("Invalid clearing number");
        }

        return getBankId(clearing.get().getBank());
    }

    public static String getBankId(ClearingNumber.Bank bank) {
        return BANK_ID_LOOKUP_TABLE.get(bank);
    }

    public static Date parseDanskeDate(String time) {
        return DateUtils.flattenTime(new Date(Long.parseLong(time.substring(6, 19))));
    }

    public static String formatDanskeDateDaily(Date date) {
        Date dailyTimeUTC = new DateTime(date).withMillisOfDay(0).withZoneRetainFields(DateTimeZone.UTC).toDate();
        return String.format("\\/Date(%d%s)\\/", dailyTimeUTC.getTime(), "+0200");
    }

    public static TransactionTypes getTinkTransactionType(String category) {
        if (category == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (category.toLowerCase()) {
        case "ou016": // Transfers to own accounts.
        case "ou017": // Other transfers (swish for example).
            return TransactionTypes.TRANSFER;
        default:
            return TransactionTypes.DEFAULT;
        }
    }
}
