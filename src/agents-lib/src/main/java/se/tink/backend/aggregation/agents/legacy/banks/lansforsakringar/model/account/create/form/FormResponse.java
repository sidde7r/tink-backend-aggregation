package se.tink.backend.aggregation.agents.banks.lansforsakringar.model.account.create.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.agents.rpc.Field;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FormResponse {

    private FormEntity form;

    public FormEntity getForm() {
        return form;
    }

    public void setForm(FormEntity form) {
        this.form = form;
    }

    // Converts the form response to a list of fields
    public List<Field> toFields() {

        List<Field> fields = Lists.newArrayList();

        if (form == null) {
            return fields;
        }

        for (SectionEntity sectionEntity : form.getSections()) {

            for (QuestionEntity questionEntity : sectionEntity.getQuestions()) {
                Field field = questionEntity.toField();

                if (field != null) {
                    fields.add(field);
                }
            }
        }

        return fields;
    }
}
