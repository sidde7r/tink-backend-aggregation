package se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FieldData {
    private final List<FieldPathPart> fieldPath;
    private final String fieldValue;
    private final JsonNodeType fieldType;
    private final List<String> offsets;
}
