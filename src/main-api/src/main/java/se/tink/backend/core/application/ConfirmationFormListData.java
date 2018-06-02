package se.tink.backend.core.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmationFormListData {

    private String title;
    private List<FieldData> fields = Lists.newArrayList();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<FieldData> getFields() {
        if (fields == null) {
            this.fields = Lists.newArrayList();
        }
        return fields;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }
    
    public void addField(FieldData field) {
        getFields().add(field);
    }
    
    public boolean isPopulated() {
        return !getFields().isEmpty();
    }
}
