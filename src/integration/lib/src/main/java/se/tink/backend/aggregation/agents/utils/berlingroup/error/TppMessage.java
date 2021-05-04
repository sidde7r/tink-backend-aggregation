package se.tink.backend.aggregation.agents.utils.berlingroup.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TppMessage {

    public static final String ERROR = "ERROR";
    public static final String WARNING = "WARNING";

    private String category;
    private String code;
    private String path;
    private String text;

    public boolean isError() {
        return ERROR.equalsIgnoreCase(category);
    }

    public boolean isWarning() {
        return WARNING.equalsIgnoreCase(category);
    }

    public boolean matches(TppMessage another) {
        return (another.category == null || another.category.equalsIgnoreCase(category))
                && (another.code == null || another.code.equalsIgnoreCase(code))
                && (another.path == null || another.path.equalsIgnoreCase(path))
                && (another.text == null || another.text.equalsIgnoreCase(text));
    }
}
