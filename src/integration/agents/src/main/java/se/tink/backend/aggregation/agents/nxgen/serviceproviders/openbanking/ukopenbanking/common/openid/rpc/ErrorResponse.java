package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Setter
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class ErrorResponse {

    private String code;

    private String message;

    private List<ErrorEntity> errors = Collections.emptyList();

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        if (errors == null) {
            return false;
        }
        return errors.stream()
                .anyMatch(errorEntity -> errorCode.equals(errorEntity.getErrorCode()));
    }

    @JsonIgnore
    public boolean messageContains(String pattern) {
        if (errors == null) {
            return false;
        }
        return errors.stream().anyMatch(errorEntity -> errorEntity.getMessage().contains(pattern));
    }

    @JsonIgnore
    public List<String> getErrorCodes() {
        if (errors == null) {
            return Collections.emptyList();
        }
        return errors.stream().map(ErrorEntity::getErrorCode).collect(Collectors.toList());
    }

    @JsonObject
    @Setter
    @Getter
    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
    private static class ErrorEntity {

        private String errorCode;

        private String message;
    }
}
