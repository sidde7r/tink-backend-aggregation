package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionGroupResponse {

    @JsonProperty("frageGrupper")
    private List<QuestionGroupEntity> questionGroups;

    public List<QuestionGroupEntity> getQuestionGroups() {
        return questionGroups;
    }

    public void setQuestionGroups(List<QuestionGroupEntity> questionGroups) {
        this.questionGroups = questionGroups;
    }
}
