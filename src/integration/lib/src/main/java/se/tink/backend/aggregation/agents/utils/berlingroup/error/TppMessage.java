package se.tink.backend.aggregation.agents.utils.berlingroup.error;

import java.util.function.BiPredicate;
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
        return matches(another, String::equalsIgnoreCase);
    }

    public boolean matches(TppMessage another, BiPredicate<String, String> bipredicate) {
        return (another.category == null
                        || (category != null && bipredicate.test(category, another.category)))
                && (another.code == null || (code != null && bipredicate.test(code, another.code)))
                && (another.path == null || (path != null && bipredicate.test(path, another.path)))
                && (another.text == null || (text != null && bipredicate.test(text, another.text)));
    }
}
