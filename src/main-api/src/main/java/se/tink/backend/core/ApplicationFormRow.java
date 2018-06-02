package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "applications_forms")
public class ApplicationFormRow {

    private String applicationId;
    @Type(type = "text")
    private String fields;
    private String status;
    @Id
    private String id;
    private String name;
    private String parentId;
    private String type;
    private Date updated;
    private String userId;
    private int formNumber;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFormNumber() {
        return formNumber;
    }

    public void setFormNumber(int formNumber) {
        this.formNumber = formNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("applicationId", applicationId)
                .add("userId", userId)
                .add("type", type)
                .add("name", name)
                .add("formNumber", formNumber)
                .add("status", status)
                .add("updated", updated)
                .add("fields", fields)
                .toString();
    }
}
