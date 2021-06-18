package se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.TemplatesEnum;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.cardreader.dto.CardReaderData;
import se.tink.backend.aggregation.agents.utils.supplementalfields.sdktemplates.commons.FieldsBuilder;

@UtilityClass
public class CardReaderTemplate {

    private static final String INSTRUCTION = "instruction";
    private static final String INPUT = "input";
    private static final String INSTRUCTION_LIST = "instructionList";

    public static List<Field> getTemplate(CardReaderData cardReaderData) {
        List<Field> templatesList = new ArrayList<>(3);
        templatesList.add(FieldsBuilder.buildTemplateField(TemplatesEnum.CARD_READER));
        templatesList.add(
                FieldsBuilder.buildInstructionField(
                        cardReaderData.getSecondFactorDescription(),
                        cardReaderData.getSecondFactorValue(),
                        INSTRUCTION));
        templatesList.add(FieldsBuilder.buildInputField(cardReaderData.getInput(), INPUT));
        templatesList.add(
                FieldsBuilder.buildInstructionsListField(
                        cardReaderData.getInstructions(), INSTRUCTION_LIST));
        return templatesList;
    }
}
