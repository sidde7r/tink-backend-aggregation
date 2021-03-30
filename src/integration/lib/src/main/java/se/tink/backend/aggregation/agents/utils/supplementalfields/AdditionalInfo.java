package se.tink.backend.aggregation.agents.utils.supplementalfields;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * This common class is to be used for passing more values to the frontend. {@link #instructions} -
 * List of instructions of the steps that user has to follow. All of the values should be earlier be
 * translated using {@link se.tink.libraries.i18n.Catalog} {@link #layoutType} - This should be one
 * of the defined enums in {@link LayoutTypes} {@link #layoutType} - Url to the additional image
 * that will be seen on frontend
 */
@Builder
@JsonObject
@JsonInclude(Include.NON_NULL)
public class AdditionalInfo {
    private List<String> instructions;
    private String imageUrl;
    private LayoutTypes layoutType;

    public enum LayoutTypes {
        INSTRUCTIONS // This type shows a simple screen for a single instruction to the user
    }

    public String serialize() {
        return SerializationUtils.serializeToString(this);
    }
}
