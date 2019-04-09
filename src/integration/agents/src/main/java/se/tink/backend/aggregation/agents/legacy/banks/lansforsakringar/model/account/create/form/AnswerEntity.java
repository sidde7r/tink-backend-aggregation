package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerEntity {

    public AnswerEntity() {}

    public AnswerEntity(String questionId, Object answer) {
        this.questionId = questionId;

        // Only support one answer right now

        if (answer == null) {
            this.answers = Lists.newArrayList();
        } else {
            this.answers = Lists.newArrayList(answer);
        }
    }

    private String questionId;

    private List<Object> answers;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public List<Object> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Object> answers) {
        this.answers = answers;
    }
}
