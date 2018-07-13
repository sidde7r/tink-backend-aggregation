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
public class QuestionGroupEntity {

    private static final AggregationLogger log = new AggregationLogger(QuestionGroupEntity.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonProperty("version")
    private double version;

    @JsonProperty("beskrivning")
    private String description;

    @JsonProperty("fragegruppId")
    private int id;

    @JsonProperty("fragetyp")
    private String questionType;

    @JsonProperty("titel")
    private String title;

    @JsonProperty("fragor")
    private List<QuestionEntity> questions;

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
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

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuestionEntity> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionEntity> questions) {
        this.questions = questions;
    }

    public static QuestionGroupEntity create(double version, int id, List<QuestionEntity> questions) {
        QuestionGroupEntity questionGroup = new QuestionGroupEntity();

        questionGroup.setVersion(version);
        questionGroup.setId(id);
        questionGroup.setQuestions(questions);

        return questionGroup;
    }

    @Override
    public boolean equals(Object compareObject) {
        if (this == compareObject) {
            return true;
        }

        if (compareObject == null || getClass() != compareObject.getClass()) {
            return false;
        }

        QuestionGroupEntity other = (QuestionGroupEntity) compareObject;

        boolean allQuestionsMatch = checkQuestionMatch(other.getQuestions());

        return Objects.equal(other.getId(), getId()) &&
                Objects.equal(other.getVersion(), getVersion()) &&
                allQuestionsMatch;
    }

    private boolean checkQuestionMatch(List<QuestionEntity> otherQuestionEntities) {
        if (otherQuestionEntities == null) {
            return false;
        }

        if (!Objects.equal(otherQuestionEntities.size(), getQuestions().size())) {
            return false;
        }

        if (!Objects.equal(otherQuestionEntities.size(), getQuestions().size())) {
            log.error("The size of the input question list (" + otherQuestionEntities.size() +
                    ") does not match the size of the list we have in store (" + getQuestions().size() + ")");
            return false;
        }

        for (final QuestionEntity otherQuestionEntity : otherQuestionEntities) {

            Optional<QuestionEntity> compareEntity = getQuestions().stream()
                    .filter(questionEntity -> Objects.equal(questionEntity.getId(), otherQuestionEntity.getId()))
                    .findFirst();

            if (!compareEntity.isPresent()) {
                log.error("A question entity with id = " + otherQuestionEntity.getId() + " was not found in store");
                logObject(otherQuestionEntity);
                return false;
            }

            if (!Objects.equal(compareEntity.get(), otherQuestionEntity)) {
                log.error("The input question entity with id = " + otherQuestionEntity.getId()
                        + " does not match the one we have in store");
                logObject(otherQuestionEntity);
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
