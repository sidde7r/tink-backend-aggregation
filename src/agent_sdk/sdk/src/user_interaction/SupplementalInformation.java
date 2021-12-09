package se.tink.agent.sdk.user_interaction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.Field;

@JsonSerialize(using = SupplementalInformationSerializer.class)
public class SupplementalInformation {
    private final ImmutableList<Field> fields;

    private SupplementalInformation(ImmutableList<Field> fields) {
        this.fields = fields;
    }

    public ImmutableList<Field> getFields() {
        return fields;
    }

    public static SupplementalInformation from(ImmutableList<Field> fields) {
        return new SupplementalInformation(fields);
    }

    public static SupplementalInformation from(Field... fields) {
        ImmutableList<Field> fieldList = ImmutableList.<Field>builder().add(fields).build();

        return from(fieldList);
    }
}
