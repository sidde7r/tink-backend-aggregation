package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.google.api.client.util.Charsets;
import com.google.api.client.util.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupEntity;
import se.tink.backend.aggregation.agents.banks.sbab.model.response.QuestionGroupResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenSavingsAccountQuestionGroups {

    private static final AggregationLogger log = new AggregationLogger(OpenSavingsAccountQuestionGroups.class);

    public static Map<Integer, QuestionGroupEntity> getAllById() {
        String jsonInput;
        try {
            jsonInput = Files.toString(new File("data/agents/sbab/question-groups-input.json"), Charsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not read json file with question group inputs");
            return Maps.newHashMap();
        }

        QuestionGroupResponse questionGroupResponse = SerializationUtils
                .deserializeFromString(jsonInput, QuestionGroupResponse.class);

        List<QuestionGroupEntity> questionGroupEntities = questionGroupResponse.getQuestionGroups();
        Map<Integer, QuestionGroupEntity> questionGroupsById = Maps.newHashMap();

        for (QuestionGroupEntity questionGroup : questionGroupEntities) {
            questionGroupsById.put(questionGroup.getId(), questionGroup);
        }

        return questionGroupsById;
    }
}
