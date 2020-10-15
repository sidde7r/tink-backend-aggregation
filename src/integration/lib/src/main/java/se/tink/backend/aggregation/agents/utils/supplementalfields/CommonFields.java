package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.stream.IntStream;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class CommonFields {

    public static class Selection {
        private static final String FIELD_KEY = "selectAuthMethodField";
        private static final LocalizableKey DESCRIPTION =
                new LocalizableKey("Authentication method index");
        private static final LocalizableParametrizedKey HINT_FORMAT =
                new LocalizableParametrizedKey("Select from 1 to {0}");
        private static final LocalizableKey PATTERN_ERROR_MESSAGE =
                new LocalizableKey("The value you entered is not valid.");

        private static final String SELECTABLE_OPTION_FORMAT = "(%d) %s";

        public static String getFieldKey() {
            return FIELD_KEY;
        }

        public static Field build(Catalog catalog, List<String> options) {
            int maxNumber = options.size();
            String helpText =
                    IntStream.range(0, maxNumber)
                            .mapToObj(
                                    index ->
                                            String.format(
                                                    SELECTABLE_OPTION_FORMAT,
                                                    index + 1,
                                                    options.get(index)))
                            .collect(joining("\n"));

            return Field.builder()
                    .name(FIELD_KEY)
                    .description(catalog.getString(DESCRIPTION))
                    .hint(catalog.getString(HINT_FORMAT, maxNumber))
                    .helpText(helpText)
                    .numeric(true)
                    .minLength(1)
                    .maxLength(Integer.toString(maxNumber).length())
                    .pattern(RangeRegex.regexForRange(1, maxNumber))
                    .patternError(catalog.getString(PATTERN_ERROR_MESSAGE))
                    .build();
        }
    }

    static class Information {
        public static Field build(
                String fieldKey, String description, String value, String helpText) {
            return Field.builder()
                    .immutable(true)
                    .name(fieldKey)
                    .description(description)
                    .value(value)
                    .helpText(helpText)
                    .build();
        }
    }
}
