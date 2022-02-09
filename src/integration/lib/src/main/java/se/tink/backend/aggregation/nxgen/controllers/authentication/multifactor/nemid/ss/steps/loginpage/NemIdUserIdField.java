package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@UtilityClass
public class NemIdUserIdField {

    public static final String NAME = "nemIdUserIdField";
    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please enter your NemID user ID");
    private static final LocalizableKey HELP_TEXT =
            new LocalizableKey(
                    "Please enter the same User ID as you would enter to NemID. It’s either NemID number, CPR number or self-chosen NemID username.");
    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 48;
    private static final String PATTERN =
            "^[a-zA-Z0-9{}!#\"$'%^&,*()_+\\-=:;?.@]{" + MIN_LENGTH + "," + MAX_LENGTH + "}$";
    private static final LocalizableKey PATTERN_ERROR =
            new LocalizableKey(
                    "Must be between 5 and 48 characters\nMay not contain certain special characters, such as æ, ø, å\nMay not begin or end with a blank character");

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
}
