package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdCprField {

    public static final String NAME = "mitIdAskUserIdField";

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please enter your CPR number");
    private static final LocalizableKey HELP_TEXT =
            new LocalizableKey(
                    "CPR number is usually required only during the very first MitID login. If you're continuously asked for it, you can authenticate on your bank's website, enter CPR and select a checkbox to remember it for all future authentications.");
    private static final int EXPECTED_VALUE_LENGTH = 10;
    private static final String HINT = StringUtils.repeat("N", EXPECTED_VALUE_LENGTH);
    private static final String PATTERN = String.format("^\\d{%d}$", EXPECTED_VALUE_LENGTH);
    private static final LocalizableKey PATTERN_ERROR =
            new LocalizableKey("CPR must be a 10 digit number");

    public static Field build(Catalog catalog) {
        return Field.builder()
                .name(NAME)
                .description(catalog.getString(DESCRIPTION))
                .helpText(catalog.getString(HELP_TEXT))
                .minLength(EXPECTED_VALUE_LENGTH)
                .maxLength(EXPECTED_VALUE_LENGTH)
                .numeric(true)
                .hint(HINT)
                .pattern(PATTERN)
                .patternError(catalog.getString(PATTERN_ERROR))
                .build();
    }

    public static void assertValidCpr(String cpr) {
        if (!Pattern.matches(PATTERN, cpr)) {
            throw MitIdError.INVALID_CPR_FORMAT.exception(PATTERN_ERROR);
        }
    }
}
