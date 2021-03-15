package se.tink.backend.aggregation.agents.utils.supplementalfields;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.utils.RangeRegex;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

@Slf4j
public class CommonFields {

    public static class Selection {
        private static final String FIELD_KEY = "selectAuthMethodField";

        private static final LocalizableParametrizedKey HINT_FORMAT =
                new LocalizableParametrizedKey("Select from 1 to {0}");
        private static final LocalizableKey PATTERN_ERROR_MESSAGE =
                new LocalizableKey("The value you entered is not valid.");
        private static final LocalizableKey DEFAULT_DESCRIPTION =
                new LocalizableKey("Choose authentication method");
        private static final String SELECTABLE_OPTION_FORMAT = "(%d) %s";

        public static String getFieldKey() {
            return FIELD_KEY;
        }

        public static Field build(Catalog catalog, List<String> options) {
            return build(catalog, options, null);
        }

        public static Field build(
                Catalog catalog, List<String> options, LocalizableKey description) {
            return build(catalog, description, prepareSelectOptions(options));
        }

        public static Field build(
                Catalog catalog, LocalizableKey description, List<SelectOption> selectOptions) {
            logOptions(selectOptions);
            int maxNumber = selectOptions.size();
            String helpText =
                    IntStream.range(0, maxNumber)
                            .mapToObj(
                                    index ->
                                            String.format(
                                                    SELECTABLE_OPTION_FORMAT,
                                                    index + 1,
                                                    selectOptions.get(index).getText()))
                            .collect(joining("\n"));

            return Field.builder()
                    .name(FIELD_KEY)
                    .description(
                            catalog.getString(
                                    description != null ? description : DEFAULT_DESCRIPTION))
                    .hint(catalog.getString(HINT_FORMAT, maxNumber))
                    .helpText(helpText)
                    .numeric(true)
                    .minLength(1)
                    .maxLength(Integer.toString(maxNumber).length())
                    .pattern(RangeRegex.regexForRange(1, maxNumber))
                    .patternError(catalog.getString(PATTERN_ERROR_MESSAGE))
                    .selectOptions(selectOptions)
                    .build();
        }

        private static void logOptions(List<SelectOption> options) {
            log.info("[SelectOption] Available methods to select: {}", options);
        }

        private static List<SelectOption> prepareSelectOptions(List<String> options) {
            return IntStream.range(0, options.size())
                    .mapToObj(
                            index ->
                                    new SelectOption(options.get(index), String.valueOf(index + 1)))
                    .collect(Collectors.toList());
        }
    }

    /**
     * This common class is to be used for displaying a piece of information, some code, reference
     * words, that user needs to proceed. Such piece of information is (usually) different for each
     * login, execution.
     */
    public static class Information {
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

    /**
     * This common class is to be used for displaying instructions. These instructions are pretty
     * much constant for each execution.
     */
    public static class Instruction {
        private static final String FIELD_KEY = "instructionField";
        // This layoutType allows tinklink to handle info-screens in a nicer way.
        // ITE-2237 for some details
        private static final String INFO_SCREEN_ADDITIONAL_INFO =
                "{\"layoutType\":\"INSTRUCTIONS\"}";

        public static Field build(String fieldKey, String value) {
            return commonBuild(value).name(fieldKey).build();
        }

        public static Field build(String value) {
            return commonBuild(value).build();
        }

        private static Field.Builder commonBuild(String value) {
            return Field.builder()
                    .description("")
                    .additionalInfo(INFO_SCREEN_ADDITIONAL_INFO)
                    .immutable(true)
                    .name(FIELD_KEY)
                    .value(value);
        }
    }

    public static class KeyCardInfo {
        public static final String FIELD_KEY = "keyCardInfoField";

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Key card index");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey("Input the code from your code card");

        public static Field build(Catalog catalog, String codeIndex, String cardId) {
            String helpText = catalog.getString(HELPTEXT);
            if (cardId != null) {
                helpText += " (" + cardId + ")";
            }
            return Information.build(
                    FIELD_KEY, catalog.getString(DESCRIPTION), codeIndex, helpText);
        }
    }

    public static class KeyCardCode {
        public static final String FIELD_KEY = "keyCardValueField";

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Key card code");

        public static Field build(Catalog catalog) {
            return commonBuild(catalog).build();
        }

        public static Field build(Catalog catalog, int expectedLength) {
            return commonBuild(catalog)
                    .minLength(expectedLength)
                    .maxLength(expectedLength)
                    .hint(StringUtils.repeat("N", expectedLength))
                    .build();
        }

        private static Field.Builder commonBuild(Catalog catalog) {
            return Field.builder()
                    .name(FIELD_KEY)
                    .description(catalog.getString(DESCRIPTION))
                    .numeric(true);
        }
    }

    public static class CodeTokenInfo {

        public static final String FIELD_KEY = "codeTokenInfoField";

        private static final LocalizableKey DESCRIPTION =
                new LocalizableKey("Code token serial number");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey("Enter the code generated with your code token");

        public static Field build(Catalog catalog, String codeTokenSerialNumber) {
            return Information.build(
                    FIELD_KEY,
                    catalog.getString(DESCRIPTION),
                    codeTokenSerialNumber,
                    catalog.getString(HELPTEXT));
        }
    }

    public static class CodeTokenCode {

        public static final String FIELD_KEY = "codeTokenCodeValueField";

        private static final Integer EXPECTED_CODE_LENGTH = 6;
        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Code token code");

        public static Field build(Catalog catalog) {
            return Field.builder()
                    .name(FIELD_KEY)
                    .description(catalog.getString(DESCRIPTION))
                    .numeric(true)
                    .minLength(EXPECTED_CODE_LENGTH)
                    .maxLength(EXPECTED_CODE_LENGTH)
                    .hint(StringUtils.repeat("N", EXPECTED_CODE_LENGTH))
                    .build();
        }
    }
}
