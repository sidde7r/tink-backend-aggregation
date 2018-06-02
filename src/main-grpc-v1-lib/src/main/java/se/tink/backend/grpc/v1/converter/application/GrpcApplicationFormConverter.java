package se.tink.backend.grpc.v1.converter.application;

import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.models.RequestApplicationForm;
import se.tink.grpc.v1.rpc.SubmitApplicationFormRequest;
import se.tink.libraries.uuid.UUIDUtils;

import java.util.stream.Collectors;

public class GrpcApplicationFormConverter implements Converter<SubmitApplicationFormRequest, ApplicationForm> {
    @Override
    public ApplicationForm convertFrom(SubmitApplicationFormRequest input) {

        ApplicationForm form = new ApplicationForm();
        form.setApplicationId(UUIDUtils.fromString(input.getApplicationId()));

        RequestApplicationForm requestForm = input.getForm();
        if (requestForm != null) {
            form.setId(UUIDUtils.fromString(requestForm.getId()));
            form.setFields(requestForm.getFields().getFieldList().stream().map(f -> {
                ApplicationField field = new ApplicationField();
                if (f.hasValue()) {
                    field.setValue(f.getValue().getValue());
                }
                field.setName(f.getName());
                return field;
            }).collect(Collectors.toList()));
        }
        return form;
    }
}
