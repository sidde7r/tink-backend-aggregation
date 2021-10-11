package se.tink.backend.aggregation.nxgen.http.event.event_producers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class FieldPathPart {

    private final String keyName;
    private boolean keyRepresentsArray;

    public FieldPathPart(FieldPathPart obj) {
        this.keyName = obj.keyName;
        this.keyRepresentsArray = obj.keyRepresentsArray;
    }
}
