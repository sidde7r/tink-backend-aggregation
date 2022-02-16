package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@Slf4j
@JsonObject
@Setter
@Getter
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class UkObErrorResponse {

    private String code;
    private String message;
    private List<ErrorEntity> errors = Collections.emptyList();

    @JsonIgnore
    public boolean isErrorCode(String errorCode) {
        if (errorCode == null) {
            return false;
        }
        return errorCode.equals(code);
    }

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        if (CollectionUtils.isEmpty(errors)) {
            return false;
        }
        log.info("[ErrorResponse] Received errors list: `{}`", errors);
        return errors.stream()
                .anyMatch(errorEntity -> errorCode.equalsIgnoreCase(errorEntity.getErrorCode()));
    }

    @JsonIgnore
    public boolean messageContains(String pattern) {
        log.info(
                "[ErrorResponse] Received errors list: `{}` with messages: `{}`",
                errors,
                getErrorMessages());
        return Optional.ofNullable(errors)
                .map(
                        listOfErrors ->
                                listOfErrors.stream()
                                        .anyMatch(
                                                errorEntity ->
                                                        errorEntity
                                                                .getMessage()
                                                                .toLowerCase()
                                                                .contains(pattern.toLowerCase())))
                .orElse(false);
    }

    @JsonIgnore
    public List<String> getErrorCodes() {
        return Optional.ofNullable(errors)
                .map(
                        listOfErrors ->
                                listOfErrors.stream()
                                        .map(ErrorEntity::getErrorCode)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @JsonIgnore
    public List<String> getErrorMessages() {
        return Optional.ofNullable(errors)
                .map(
                        listOfErrors ->
                                listOfErrors.stream()
                                        .map(ErrorEntity::getMessage)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @JsonIgnore
    public List<String> getErrorPaths() {
        return Optional.ofNullable(errors)
                .map(
                        listOfErrors ->
                                listOfErrors.stream()
                                        .map(ErrorEntity::getPath)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @JsonObject
    @Setter
    @Getter
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    private static class ErrorEntity {

        private String errorCode;

        private String message;

        private String path;
    }
}
