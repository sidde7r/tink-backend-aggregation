package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import se.tink.backend.common.utils.FunctionUtils;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = IdentityStringConverterFactory.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LowerCaseStringConverterFactory.class, name = "tolower"),
        @JsonSubTypes.Type(value = LowerCaseOnlyAZStringConverterFactory.class, name = "toloweronlyaz"),
        @JsonSubTypes.Type(value = LowerCaseNoDashesNoLoneDigitsStringConverterFactory.class, name = "tolowernodashesnolonedigits"),
        @JsonSubTypes.Type(value = LowerCaseNorwegianCleanupStringConverterFactory.class, name = "norwegianformat"),
        @JsonSubTypes.Type(value = LowerCaseDenmarkCleanupStringConverterFactory.class, name = "denmarkformat"),
        @JsonSubTypes.Type(value = StripCurvePrefixStringConverterFactory.class, name = "stripcurveprefix")
})
public abstract class StringConverterFactory {
    protected final ImmutableList.Builder<Function<String, String>> builder = ImmutableList.<Function<String, String>>builder();

    public Function<String, String> build() {
        return new FunctionUtils<String>().compose(builder.build());
    }
}
