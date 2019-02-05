package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.agents.rpc.Field;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionEntity {

    private String title;
    private String subtitle;
    private String id;
    private TypeEntity type;
    private Boolean mandatory;
    private Boolean question;
    private Object placeholder;
    private String footNote;
    private Object infoText;
    private String url;
    private String iconType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TypeEntity getType() {
        return type;
    }

    public void setType(TypeEntity type) {
        this.type = type;
    }

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public Boolean getQuestion() {
        return question;
    }

    public void setQuestion(Boolean question) {
        this.question = question;
    }

    public Object getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(Object placeholder) {
        this.placeholder = placeholder;
    }

    public String getFootNote() {
        return footNote;
    }

    public void setFootNote(String footNote) {
        this.footNote = footNote;
    }

    public Object getInfoText() {
        return infoText;
    }

    public void setInfoText(Object infoText) {
        this.infoText = infoText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public Field toField() {

        if (type == null) {
            return null;
        }

        Field field = new Field();
        field.setName(id);
        field.setDescription(title);
        field.setOptional(!mandatory);
        field.setNumeric(type.isNumeric());

        return field;
    }
}

