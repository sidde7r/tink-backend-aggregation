package se.tink.backend.core.follow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterables;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Creatable;
import se.tink.backend.core.Modifiable;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "follow_items")
public class FollowItem implements Cloneable {
    @Creatable
    @Modifiable
    @Column(length = 1024)
    @Tag(1)
    @ApiModelProperty(name = "criteria", value="A serialized json string with the criteria for the Follow Item. For all types of Follow Items, there is the parameter targetAmount that sets the goal of budget/goal. For EXPENSES there is also categoryIds which is an array of category id strings. For SEARCH there is queryString, that is the search query that would be followed. Finally, for SAVINGS there is the string targetPeriod (yyyy-mm) which sets the goal month, and the array of strings accountIds.", example = "{\"targetAmount\":500,\"categoryIds\":[\"c0d99a4058854b8681154ab91c3c5830\"]}", required = true)
    private String criteria;
    @Exclude
    @ApiModelProperty(name = "created", hidden = true)
    private Date created;
    @Transient
    @Tag(2)
    @ApiModelProperty(name = "data", value="Returned when getting one FollowItem. Contains statistics for the queried period.")
    private FollowData data;
    @Id
    @Tag(3)
    @ApiModelProperty(name = "id", value="The id of the Follow Item.", example = "e2b746ed27c542ce846a8d693474df21")
    private String id;
    @Exclude
    @ApiModelProperty(name = "lastModified", hidden = true)
    private Date lastModified;
    @Creatable
    @Modifiable
    @Tag(4)
    @ApiModelProperty(name = "name", value="The name of the Follow Item.", example = "Coffee budget", required = true)
    private String name;
    @Creatable
    @Enumerated(EnumType.STRING)
    @Tag(5)
    @ApiModelProperty(name = "type", value="The type of the Follow Item.", allowableValues = FollowTypes.DOCUMENTED, example = "EXPENSES", required = true)
    private FollowTypes type;
    @JsonIgnore
    @Exclude
    private String userId;

    @Exclude
    @JsonIgnore
    @Transient
    private FollowCriteria followCriteria;

    public FollowItem() {
        id = StringUtils.generateUUID();
    }

    @Override
    public FollowItem clone() {
        try {
            return (FollowItem) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Date getCreated() {
        return created;
    }

    public String getCriteria() {
        return criteria;
    }

    public FollowData getData() {
        return data;
    }

    public String getId() {
        return id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public Double getProgress() {
        FollowCriteria c = getFollowCriteria();

        if (c.getTargetAmount() == null) {
            return null;
        }

        if (getData().getHistoricalAmounts() == null) {
            return 0d;
        }

        Double amount = Iterables.getLast(getData().getHistoricalAmounts()).getValue();

        if (amount == null) {
            return 0d;
        }

        return (amount / c.getTargetAmount());
    }

    public FollowTypes getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    @JsonIgnore
    public boolean isProgressPositive() {
        Double progress = getProgress();

        return (progress == null || progress < 1);
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
        this.followCriteria = SerializationUtils.deserializeFromString(criteria, FollowCriteria.class);
    }

    public void setData(FollowData data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(FollowTypes type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFollowCriteria(FollowCriteria followCriteria) {
        this.followCriteria = followCriteria;
        this.criteria = SerializationUtils.serializeToString(followCriteria);
    }

    public FollowCriteria getFollowCriteria() {
        if (followCriteria == null) {
            followCriteria = SerializationUtils.deserializeFromString(criteria, FollowCriteria.class);
        }
        return followCriteria;
    }
}
