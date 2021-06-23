package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.TemplatesEnum;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.CommonPositionalInput;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.dto.InGroup;

public class FieldsBuilder {
    private static final String INFO_SCREEN_ADDITIONAL_INFO = "{\"layoutType\":\"INSTRUCTIONS\"}";

    public static Field buildTemplateField(TemplatesEnum templateName) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.TinkLinkCompatible.TEMPLATE)
                .description(CommonConstants.FieldTypes.TinkLinkCompatible.TEMPLATE)
                .immutable(true)
                .name(CommonConstants.FieldTypes.TinkLinkCompatible.TEMPLATE)
                .value(templateName.toString())
                .build();
    }

    public static Field buildIconField(String iconUrl, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.TinkLinkCompatible.ICON)
                .description(CommonConstants.FieldTypes.TinkLinkCompatible.ICON)
                .immutable(true)
                .name(name)
                .value(iconUrl)
                .build();
    }

    public static Field buildIdentityHint(String hintImage, String hintText, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.TinkLinkCompatible.ICON)
                .style(CommonConstants.FieldStyles.TinkLinkCompatible.IDENTITY_HINT)
                .description(hintText)
                .immutable(true)
                .name(name)
                .value(hintImage)
                .build();
    }

    public static Field buildInputField(CommonInput commonInput, String name) {
        InGroup inGroup = commonInput.getInGroup();
        return Field.builder()
                .description(commonInput.getDescription())
                .type(CommonConstants.FieldTypes.BackwardCompatible.INPUT)
                .style(CommonConstants.FieldTypes.BackwardCompatible.INPUT)
                .group(inGroup != null ? inGroup.getGroup() : null)
                .helpText(commonInput.getInputFieldHelpText())
                .hint(StringUtils.repeat("N", commonInput.getInputFieldMaxLength()))
                .maxLength(commonInput.getInputFieldMaxLength())
                .minLength(commonInput.getInputFieldMinLength())
                .name(name)
                .oneOf(inGroup != null && inGroup.isOneOf())
                .patternError(commonInput.getInputFieldPatternError())
                .pattern(commonInput.getInputFieldPattern())
                .sensitive(commonInput.isSensitive())
                .build();
    }

    public static Field buildPositionalInputField(
            CommonPositionalInput commonPositionalInput, String name) {
        InGroup inGroup = commonPositionalInput.getInGroup();
        return Field.builder()
                .type(CommonConstants.FieldTypes.BackwardCompatible.INPUT)
                .style(CommonConstants.FieldStyles.TinkLinkCompatible.POSITIONAL_INPUT)
                .description(commonPositionalInput.getDescription())
                .group(inGroup != null ? inGroup.getGroup() : null)
                .helpText(commonPositionalInput.getInputFieldHelpText())
                .hint(commonPositionalInput.getHint())
                .maxLength(commonPositionalInput.getInputFieldMaxLength())
                .minLength(commonPositionalInput.getInputFieldMinLength())
                .name(name)
                .oneOf(inGroup != null && inGroup.isOneOf())
                .patternError(commonPositionalInput.getInputFieldPatternError())
                .pattern(commonPositionalInput.getInputFieldPattern())
                .sensitive(commonPositionalInput.isSensitive())
                .build();
    }

    public static Field buildInstructionsListField(List<String> instructions, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.BackwardCompatible.TEXT)
                .style(CommonConstants.FieldStyles.TinkLinkCompatible.ORDERED_LIST)
                .description(CommonConstants.FieldStyles.TinkLinkCompatible.ORDERED_LIST)
                .immutable(true)
                .name(name)
                .value(instructions.toString())
                .build();
    }

    public static Field buildInstructionField(
            String instructionDescription, String instructionValue, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.BackwardCompatible.TEXT)
                .style(CommonConstants.FieldStyles.BackwardCompatible.INSTRUCTION)
                .description(Strings.nullToEmpty(instructionDescription))
                .additionalInfo(INFO_SCREEN_ADDITIONAL_INFO)
                .immutable(true)
                .name(name)
                .value(instructionValue)
                .build();
    }

    public static Field buildTitleField(String title, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.BackwardCompatible.TEXT)
                .style(CommonConstants.FieldStyles.TinkLinkCompatible.TITLE)
                .name(name)
                .description(CommonConstants.FieldStyles.TinkLinkCompatible.TITLE)
                .immutable(true)
                .value(title)
                .build();
    }

    public static Field buildColorField(String colorHex, String name) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.TinkLinkCompatible.COLOR)
                .description(CommonConstants.FieldTypes.TinkLinkCompatible.COLOR)
                .name(name)
                .immutable(true)
                .value(colorHex)
                .build();
    }

    public static Field buildChangeMethodField(String buttonText) {
        return Field.builder()
                .type(CommonConstants.FieldTypes.TinkLinkCompatible.CHANGE_METHOD)
                .description(CommonConstants.FieldTypes.TinkLinkCompatible.CHANGE_METHOD)
                .name(CommonConstants.FieldTypes.TinkLinkCompatible.CHANGE_METHOD)
                .value(buttonText)
                .immutable(true)
                .build();
    }

    public static List<Field> buildChooseInputFields(
            List<? extends CommonInput> identifications, String coreName) {
        validateChooseInputs(identifications);
        List<Field> chooseFields = new ArrayList<>();
        CommonInput input = identifications.get(0);
        if (input instanceof CommonPositionalInput) {
            IntStream.of(0, identifications.size() - 1)
                    .forEach(
                            i ->
                                    chooseFields.add(
                                            buildPositionalInputField(
                                                    (CommonPositionalInput) identifications.get(i),
                                                    coreName + i)));

        } else {
            IntStream.of(0, identifications.size() - 1)
                    .forEach(
                            i ->
                                    chooseFields.add(
                                            buildInputField(identifications.get(i), coreName + i)));
        }
        return chooseFields;
    }

    private static void validateChooseInputs(List<? extends CommonInput> identifications) {
        if (CollectionUtils.isEmpty(identifications)) {
            throw new IllegalStateException("No fields in the group");
        }

        InGroup inGroup1 = identifications.get(0).getInGroup();
        String firstGroupName = inGroup1 != null ? inGroup1.getGroup() : null;
        boolean firstOneOf = inGroup1 != null && inGroup1.isOneOf();
        for (CommonInput commonInput : identifications) {
            InGroup inGroup = commonInput.getInGroup();
            if (inGroup == null) {
                throw new IllegalStateException("Fields in group not filled");
            }
            String group = inGroup.getGroup();
            if (StringUtils.isBlank(group)) {
                throw new IllegalStateException("Group has not set name (group)");
            }
            if (!group.equals(firstGroupName)) {
                throw new IllegalStateException("Group does not have the same names");
            }
            if (BooleanUtils.compare(inGroup.isOneOf(), firstOneOf) != 0) {
                throw new IllegalStateException("Group does not have the same one of");
            }
        }
    }
}
