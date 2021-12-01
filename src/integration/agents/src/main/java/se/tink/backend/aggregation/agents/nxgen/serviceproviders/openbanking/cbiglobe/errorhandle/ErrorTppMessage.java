package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle;

import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorTppMessage {
    private String code;
    private String text;
    private String category;

    public boolean isError() {
        return "ERROR".equalsIgnoreCase(category);
    }

    public boolean isSameError(String code, String text) {
        return isError() && Objects.equals(this.code, code) && Objects.equals(this.text, text);
    }
}
