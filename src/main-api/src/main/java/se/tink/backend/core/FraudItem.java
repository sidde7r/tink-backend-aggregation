package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "fraud_items")
public class FraudItem implements Cloneable {

    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    @Tag(1)
    private String description;
    @Tag(2)
    private String id;
    @Tag(9)
    private int sortOrder;
    @Tag(7)
    private List<String> sources;
    @Exclude
    private String sourcesSerialized;
    @Tag(3)
    private FraudStatus status;
    @Tag(4)
    private FraudTypes type;
    @Tag(8)
    private int unhandledDetailsCount;
    @Tag(5)
    private Date updated; // Updated is the time when there is a new FraudDetails for the item.
    @Tag(6)
    private String userId;
    @Tag(10)
    private int unseenDetailsCount;

    public FraudItem() {
        id = StringUtils.generateUUID();
    }

    @Override
    public FraudItem clone() {
        try {
            return (FraudItem) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Transient
    public String getDescription() {
        return description;
    }

    @Id
    public String getId() {
        return id;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    @Transient
    public List<String> getSources() {
        if (!Strings.isNullOrEmpty(sourcesSerialized)) {
            return SerializationUtils.deserializeFromString(sourcesSerialized, STRING_LIST_TYPE_REFERENCE);
        }
        return null;
    }

    @JsonIgnore
    @Column(name = "`sources`")
    @Type(type = "text")
    public String getSourcesSerialized() {
        if (sources != null) {
            return SerializationUtils.serializeToString(sources);
        } else {
            return sourcesSerialized;
        }
    }

    @Enumerated(EnumType.STRING)
    public FraudStatus getStatus() {
        return status;
    }

    @Enumerated(EnumType.STRING)
    public FraudTypes getType() {
        return type;
    }

    public int getUnhandledDetailsCount() {
        return unhandledDetailsCount;
    }

    public Date getUpdated() {
        return updated;
    }

    public String getUserId() {
        return userId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSources(List<String> sources) {
        if (sources == null) {
            return;
        }

        this.sources = sources;

        if (sources != null) {
            sourcesSerialized = SerializationUtils.serializeToString(sources);
        }
    }

    public void setSourcesSerialized(String sourcesSerialized) {
        if (Strings.isNullOrEmpty(sourcesSerialized)) {
            return;
        }

        this.sourcesSerialized = sourcesSerialized;

        if (!Strings.isNullOrEmpty(sourcesSerialized)) {
            sources = SerializationUtils.deserializeFromString(sourcesSerialized, STRING_LIST_TYPE_REFERENCE);
        }
    }

    public void setStatus(FraudStatus status) {
        this.status = status;
    }

    public void setType(FraudTypes type) {
        this.type = type;
    }

    public void setUnhandledDetailsCount(int unhandledDetailsCount) {
        this.unhandledDetailsCount = unhandledDetailsCount;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getUnseenDetailsCount() {
        return unseenDetailsCount;
    }

    public void setUnseenDetailsCount(int unseenDetailsCount) {
        this.unseenDetailsCount = unseenDetailsCount;
    }
}
