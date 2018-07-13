package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionEntity {

    private static final AggregationLogger log = new AggregationLogger(QuestionEntity.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("forklarandetext")
    private String description;

    @JsonProperty("frageId")
    private int id;

    @JsonProperty("fragetext")
    private String question;

    @JsonProperty("svarsalternativ")
    private List<AnswerEntity> answers;

    @JsonProperty("tekniskNyckel")
    private String key;

    @JsonProperty("fritextTillaten")
    private boolean customValueAllowed;

    public boolean isCustomValueAllowed() {
        return customValueAllowed;
    }

    public void setCustomValueAllowed(boolean customValueAllowed) {
        this.customValueAllowed = customValueAllowed;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<AnswerEntity> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerEntity> answers) {
        this.answers = answers;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object compareObject) {
        if (this == compareObject) {
            return true;
        }

        if (compareObject == null || getClass() != compareObject.getClass()) {
            return false;
        }

        QuestionEntity other = (QuestionEntity) compareObject;

        boolean allAnswerAlternativesMatch = checkAnswerMatch(other.getAnswers());

        return Objects.equal(getId(), other.getId()) &&
                Objects.equal(isCustomValueAllowed(), other.isCustomValueAllowed()) &&
                Objects.equal(getKey(), other.getKey()) &&
                Objects.equal(getQuestion(), other.getQuestion()) &&
                allAnswerAlternativesMatch;
    }

    private boolean checkAnswerMatch(List<AnswerEntity> answers) {
        if (answers == null) {
            return false;
        }

        if (!Objects.equal(answers.size(), getAnswers().size())) {
            log.error("The size of the input answer list (" + answers.size() +
                    ") does not match the size of the list we have in store (" + getAnswers().size() + ")");
            return false;
        }

        for (final AnswerEntity otherAnswerEntity : answers) {

            Optional<AnswerEntity> compareEntity = getAnswers().stream()
                    .filter(answerEntity -> Objects.equal(answerEntity.getId(), otherAnswerEntity.getId()))
                    .findFirst();

            if (!compareEntity.isPresent()) {
                log.error("An answer entity with id = " + otherAnswerEntity.getId() + " was not found in store");
                logObject(otherAnswerEntity);
                return false;
            }

            if (!Objects.equal(compareEntity.get(), otherAnswerEntity)) {
                log.error("The input answer entity with id = " + otherAnswerEntity.getId()
                        + " does not match the one we have in store");
                logObject(otherAnswerEntity);
                return false;
            }
        }

        return true;
    }

    private void logObject(Object object) {
        try {
            log.error(MAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
