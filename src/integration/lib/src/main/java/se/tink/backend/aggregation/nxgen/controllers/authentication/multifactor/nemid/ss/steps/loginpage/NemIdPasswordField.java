package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.loginpage;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@UtilityClass
public class NemIdPasswordField {

    public static final String NAME = "nemIdPasswordField";
    private static final LocalizableKey DESCRIPTION =
            new LocalizableKey("Please enter your NemID password");
    private static final LocalizableKey HELP_TEXT =
            new LocalizableKey(
                    "Please enter your password to NemID. It can be a 4 digit password or password with at least 6 alphanumeric characters. If you do not remember it, you can reset your password on NemID website.");
    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 40;
    private static final String PATTERN = "^(\\d{4}|[a-zA-Z0-9{}!#\"$'%^&,*()_+\\-=:;?.@]{6,40})$";
    private static final LocalizableKey PATTERN_ERROR =
            new LocalizableKey(
                    "Must be between 4 and 40 characters\nMay not begin or end with a blank character\nMay not contain certain special characters, such as æ, ø, å");

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
