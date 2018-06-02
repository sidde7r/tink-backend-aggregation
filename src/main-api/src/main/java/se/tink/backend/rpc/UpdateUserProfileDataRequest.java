package se.tink.backend.rpc;

import io.protostuff.Tag;

import java.util.List;
import se.tink.backend.core.Field;

public class UpdateUserProfileDataRequest {

    @Tag(1)
    private List<Field> fields;

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
