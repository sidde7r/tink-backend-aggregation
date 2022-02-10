package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.fields;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.mitid.MitIdError;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdUserIdField {

    public static final String NAME = "mitIdAskUserIdField";

    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please enter your MitID user ID");
    private static final LocalizableKey HELP_TEXT =
            new LocalizableKey(
                    "Your MitID user ID is the name you are identified by when you log on or approve using MitID.");
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 48;
    private static final String PATTERN =
            String.format(
                    "^[abcdefghijklmnopqrstuvwxyzæøå0123456789{}!#$^,*()_+-=:;?.@ ]{%d,%d}$",
                    MIN_LENGTH, MAX_LENGTH);
    protected static final LocalizableKey PATTERN_ERROR =
            new LocalizableKey(
                    "User ID must be made up of the following symbols: abcdefghijklmnopqrstuvwxyzæøå0123456789{}!#$^,*()_+-=:;?.@ and spacebar.");

    public static Field build(Catalog catalog) {
        return Field.builder()
                .name(NAME)
                .description(catalog.getString(DESCRIPTION))
                .helpText(catalog.getString(HELP_TEXT))
                .minLength(MIN_LENGTH)
                .maxLength(MAX_LENGTH)
                .pattern(PATTERN)
                .patternError(catalog.getString(PATTERN_ERROR))
                .build();
    }

    public static void assertValidUserId(String userId) {
        if (!Pattern.matches(PATTERN, userId)) {
            throw MitIdError.INVALID_USER_ID_FORMAT.exception(PATTERN_ERROR);
        }
    }
}
