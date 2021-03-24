package se.tink.backend.aggregation.agents.banks.seb.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.seb.SebAccountIdentifierFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Bank;

/**
 * BankPrefixes reverse engineered from the SEB's webapp (from "wow4502.aspx"). The
 * `mobileAppBankCode` were extracted by entering accounts into the SEB mobile app, listening to the
 * HTTPS traffix and extracting the code from the JSON blobs.
 */
public class BankPrefix {

    private static final AccountIdentifierFormatter formatter = new SebAccountIdentifierFormatter();

    private static final ImmutableMap<ClearingNumber.Bank, String> lookup =
            ImmutableMap.<ClearingNumber.Bank, String>builder()
                    .put(Bank.DANSKE_BANK, "DB")
                    .put(Bank.SKANDIABANKEN, "SKB")
                    .put(Bank.LANSFORSAKRINGAR_BANK, "LF")
                    .put(Bank.HANDELSBANKEN, "SHB")
                    .put(Bank.NORDEA, "NB")
                    .put(Bank.SWEDBANK, "SWED")
                    .put(Bank.ICA_BANKEN, "ICAB")
                    .put(Bank.SEB, "SEB")
                    .put(Bank.AMFA_BANK, "AMFA")
                    .put(Bank.AVANZA_BANK, "AZA")
                    .put(Bank.FORTIS_BANK, "BNPPF")
                    .put(Bank.BLUESTEP_FINANS, "BSTP")
                    .put(Bank.CITIBANK, "CITI")
                    .put(Bank.DANSKE_BANK_SVERIGE, "DB")
                    .put(Bank.DEN_NORSKE_BANK_SVERIGE, "DNBSE")
                    .put(Bank.ERIK_PENSER_BANKAKTIEBOLAG, "ERPB")
                    .put(Bank.FOREX_BANK, "FOREX")
                    .put(Bank.SANTANDER_CONSUMER_BANK, "GEMB")
                    .put(Bank.IKANO_BANK, "IKANO")
                    .put(Bank.LANDSHYPOTEK, "LAHYP")
                    .put(Bank.LAN_O_SPAR_BANK, "LSBSE")
                    .put(Bank.MARGINALEN_BANK, "MARG")
                    .put(Bank.NORDEA_PERSONKONTO, "NB")
                    .put(Bank.NORDNET_BANK, "NON")
                    .put(Bank.PLUSGIROT, "PGBANK")
                    .put(Bank.PLUSGIROT_BANK, "PGBANK")
                    .put(Bank.RESURS_BANK, "RB")
                    .put(Bank.ROYAL_BANK_OF_SCOTLAND, "RBS")
                    .put(Bank.RIKSGALDEN, "RGK")
                    .put(Bank.SBAB, "SBAB")
                    .put(Bank.SPARBANKEN_SYD, "SYD")
                    .put(Bank.ALANDSBANKEN, "ÅLAND")
                    .put(Bank.NORDAX_BANK, "NDX")
                    .put(Bank.HSB_BANK, "SWED") // HSB partnered with Swedbank.
                    .put(
                            Bank.EKOBANKEN,
                            "PGBANK") // PGBANK is partnered with Nordea. Ekobanken is partnered
                    // with Nordea.
                    .put(
                            Bank.JAKBANKEN,
                            "PGBANK") // PGBANK is partnered with Nordea. Jakbanken is partnered
                    // with Nordea.
                    .build();

    public static String fromAccountIdentifier(AccountIdentifier identifier) {
        Preconditions.checkArgument(
                identifier.is(AccountIdentifierType.SE),
                "Only swedish banks are handled at the moment.");

        if (identifier.getIdentifier(formatter).matches("^(4993|336[3-7])\\d{6}$")
                || identifier
                        .getIdentifier(formatter)
                        .matches("^6044993\\d{6}$|^604336[3-7]\\d{6}$")) {
            // Workaround for SEB not knowing the correct clearing number to Nordea/Danske Bank.
            // We've notified them
            // about this issue and will remove this when they get their shit together.

            return "DB";
        } else {
            Bank sebDestinationIdentifierBank = identifier.to(SwedishIdentifier.class).getBank();
            Optional<String> bankPrefix =
                    BankPrefix.fromClearingNumber(sebDestinationIdentifierBank);
            if (bankPrefix.isPresent()) {
                return bankPrefix.get();
            } else {
                throw new IllegalArgumentException(
                        "Could not BankPrefix for SEB destination identifier: "
                                + sebDestinationIdentifierBank);
            }
        }
    }

    private static Optional<String> fromClearingNumber(Bank bank) {
        return Optional.ofNullable(lookup.get(bank));
    }
}
