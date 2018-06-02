package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "fraud_details")
public class FraudDetails implements Cloneable {

    private static final LogUtils log = new LogUtils(FraudDetails.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectMapper MAPPER_FOR_PROTOBUF = new ObjectMapper();

    static {
        MAPPER.enableDefaultTyping();
    }

    @Column(name = "`content`")
    @Type(type = "text")
    @Exclude
    private String contentSerialized;
    @Tag(1)
    @JsonIgnore
    @Transient
    private String contentSerializedForProtobuf;
    @Tag(2)
    private Date date;
    @Transient
    @Tag(3)
    private String description;
    @Tag(4)
    private String fraudItemId;
    @Tag(5)
    @Id
    private String id;
    @Tag(6)
    @Enumerated(EnumType.STRING)
    private FraudStatus status;
    @Transient
    @Tag(7)
    private String title;
    @Tag(8)
    @Enumerated(EnumType.STRING)
    private FraudDetailsContentType type;
    @Tag(9)
    private String userId;
    @Exclude
    private Date created;
    @Tag(10)
    private Date updated;
    @Transient
    @Tag(11)
    private String descriptionTitle;
    @Transient
    @Tag(12)
    private String descriptionBody;
    @Transient
    @Tag(13)
    private String question;
    @Transient
    @Tag(14)
    private List<FraudDetailsAnswer> answers;

    public FraudDetails() {
        id = StringUtils.generateUUID();
    }

    @Override
    public FraudDetails clone() {
        try {
            return (FraudDetails) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    @JsonProperty("content")
    public FraudDetailsContent getContent() {
        if (!Strings.isNullOrEmpty(contentSerialized)) {
            try {
                FraudDetailsContent content = MAPPER.readValue(contentSerialized, FraudDetailsContent.class);
                contentSerializedForProtobuf = MAPPER_FOR_PROTOBUF.writeValueAsString(content);
                return content;
            } catch (Exception e) {
                log.error("Could not deserialize fraud details", e);
            }
        }
        return null;
    }

    public String getContentSerializedForProtobuf() {
        return contentSerializedForProtobuf;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getFraudItemId() {
        return fraudItemId;
    }

    public FraudStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public FraudDetailsContentType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    @JsonProperty("content")
    public void setContent(FraudDetailsContent details) {
        try {
            this.contentSerialized = MAPPER.writeValueAsString(details);
            this.contentSerializedForProtobuf = MAPPER_FOR_PROTOBUF.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize fraud details", e);
        }
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFraudItemId(String fraudItemId) {
        this.fraudItemId = fraudItemId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStatus(FraudStatus status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(FraudDetailsContentType type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public String getDescriptionBody() {
        return descriptionBody;
    }

    public void setDescriptionBody(String descriptionBody) {
        this.descriptionBody = descriptionBody;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<FraudDetailsAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<FraudDetailsAnswer> answers) {
        this.answers = answers;
    }

    public boolean isSeen() {
        switch(status){
        case SEEN:
        case OK:
        case FRAUDULENT:
            return true;
        default:
            return false;
        }
    }

    public boolean isStatusEmpty() {
        if (Objects.equals(status, FraudStatus.EMPTY)) {
            return true;
        }
        return false;
    }
}
