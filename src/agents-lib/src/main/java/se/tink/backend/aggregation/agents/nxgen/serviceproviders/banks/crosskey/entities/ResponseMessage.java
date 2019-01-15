package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.entities;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ResponseMessage {
    private boolean success;
    private List<String> errors;
    private List<String> infos;
    private String jSessionId;

    public boolean hasAnyErrors(String... errorMessages) {
        return errors.stream().anyMatch(
                Arrays.stream(errorMessages).<Predicate<String>>map(
                        errorMessage ->
                                error -> errorMessage.equalsIgnoreCase(error)
                ).reduce(Predicate::or).orElse(t -> false));
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getInfos() {
        return infos;
    }

    public void setInfos(List<String> infos) {
        this.infos = infos;
    }

    public String getjSessionId() {
        return jSessionId;
    }

    public void setjSessionId(String jSessionId) {
        this.jSessionId = jSessionId;
    }
}
