package se.tink.libraries.account.identifiers;

import com.google.common.collect.ImmutableList;

public class SepaEurIdentifier extends IbanIdentifier {
    private static ImmutableList<String> sepaCountriesWithEur =
            ImmutableList.of(
                    "AT", "BE", "CY", "DE", "EE", "ES", "FI", "FR", "GR", "IE", "IT", "LT", "LU",
                    "LV", "MT", "NL", "PT", "SI", "SK");

    public SepaEurIdentifier(final String iban) {
        super(null, iban, sepaCountriesWithEur);
    }

    @Override
    public Type getType() {
        return Type.SEPA_EUR;
    }
}
