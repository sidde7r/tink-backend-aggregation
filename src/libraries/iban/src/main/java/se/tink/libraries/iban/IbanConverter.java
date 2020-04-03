package se.tink.libraries.iban;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static lombok.AccessLevel.PRIVATE;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class IbanConverter {

    private static final Set<String> VALID_COUNTRY_CODES = newHashSet(Locale.getISOCountries());
    private static final int IBAN_CHARACTERS_OFFSET = -55;
    private static final BigInteger MODULUS = BigInteger.valueOf(97);
    private static final int CHECKSUM_BASE = 98;

    /**
     * Returns IBAN value for given country code and BBAN
     *
     * @param isoCountryCode country code in format ISO-3166 alpha-2
     * @param bban BBAN string
     * @return IBAN string
     */
    public static String getIban(String isoCountryCode, String bban) {
        String countryPrefix = getCountryPrefix(isoCountryCode);
        String checksum = calculateChecksum(countryPrefix, bban);
        return format("%s%s%s", countryPrefix, checksum, bban);
    }

    private static String getCountryPrefix(String isoCountryCode) {
        return Optional.of(isoCountryCode)
                .map(String::toUpperCase)
                .filter(VALID_COUNTRY_CODES::contains)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Provided country code should be in format ISO-3166 alpha-2"));
    }

    private static String calculateChecksum(String countryPrefix, String bban) {
        return Optional.of(getRearrangedIban(countryPrefix, bban))
                .map(IbanConverter::translateCharacters)
                .map(IbanConverter::calculateChecksum)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Couldn't calculate IBAN checksum for given params"));
    }

    private static String getRearrangedIban(String countryPrefix, String bban) {
        return format("%s%s%s", bban, countryPrefix, "00");
    }

    private static String translateCharacters(String iban) {
        return iban.toUpperCase()
                .chars()
                .mapToObj(IbanConverter::mapCharacter)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private static String calculateChecksum(String translatedIban) {
        BigInteger ibanBigIntValue = new BigInteger(translatedIban);
        int checksum = CHECKSUM_BASE - ibanBigIntValue.mod(MODULUS).intValue();
        return format("%02d", checksum);
    }

    private static String mapCharacter(int input) {
        char charValue = (char) input;
        if (Character.isDigit(charValue)) {
            return String.valueOf(charValue);
        }
        return String.valueOf(input + IBAN_CHARACTERS_OFFSET);
    }
}
