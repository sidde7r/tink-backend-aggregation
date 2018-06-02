package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessages {
    protected List<ErrorMessage> general;
    protected List<ErrorMessage> fields;

    public List<ErrorMessage> getGeneral() {
        return general;
    }

    public void setGeneral(List<ErrorMessage> general) {
        this.general = general;
    }

    public List<ErrorMessage> getFields() {
        return fields;
    }

    public void setFields(List<ErrorMessage> fields) {
        this.fields = fields;
    }

    public List<ErrorMessage> getAll() {
        List<ErrorMessage> all = Lists.newArrayList();

        if (general != null) {
            all.addAll(general);
        }

        if (fields != null) {
            all.addAll(fields);
        }

        return all;

    }

    public boolean hasGeneralErrorWithCode(String statusCode) {
        if (general == null) {
            return false;
        }

        return FluentIterable
                .from(general)
                .anyMatch(errorCodeEquals(statusCode));
    }

    private static Predicate<ErrorMessage> errorCodeEquals(final String statusCode) {
        return errorMessage -> errorMessage != null && Objects.equal(errorMessage.getCode(), statusCode);
    }

    @Override
    public String toString() {
        StringBuilder errorMessages = new StringBuilder();
        if (fields == null) {
            return errorMessages.toString();
        }

        errorMessages.append("Fields:");

        int count1 = 1;
        for (ErrorMessage field : fields) {
            errorMessages.append("ErrorMessage ");
            errorMessages.append(count1);
            errorMessages.append(": ");
            errorMessages.append(field.getMessage());
            errorMessages.append(", ");
            errorMessages.append("ErrorCode ");
            errorMessages.append(count1);
            errorMessages.append(": ");
            errorMessages.append(field.getCode());
            errorMessages.append(", ");
            errorMessages.append("RefId ");
            errorMessages.append(count1);
            errorMessages.append(": ");
            errorMessages.append(field.getRefId());
            errorMessages.append(", ");
            errorMessages.append("Field ");
            errorMessages.append(count1);
            errorMessages.append(": ");
            errorMessages.append(field.getField());
            errorMessages.append("\n");
            count1++;
        }

        errorMessages.append("General:");

        int count2 = 1;
        for (ErrorMessage gen : general) {
            errorMessages.append("ErrorMessage ");
            errorMessages.append(count2);
            errorMessages.append(": ");
            errorMessages.append(gen.getMessage());
            errorMessages.append(", ");
            errorMessages.append("ErrorCode ");
            errorMessages.append(count2);
            errorMessages.append(": ");
            errorMessages.append(gen.getCode());
            errorMessages.append(", ");
            errorMessages.append("RefId ");
            errorMessages.append(count2);
            errorMessages.append(": ");
            errorMessages.append(gen.getRefId());
            errorMessages.append(", ");
            errorMessages.append("Field ");
            errorMessages.append(count2);
            errorMessages.append(": ");
            errorMessages.append(gen.getField());
            errorMessages.append("\n");
            count2++;
        }

        return errorMessages.toString();
    }
}
