package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form.create;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form.AnswerEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAccountRequest {

    @JsonProperty("answerWrappers")
    private List<AnswerEntity> answers;

    public List<AnswerEntity> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerEntity> answers) {
        this.answers = answers;
    }
}

