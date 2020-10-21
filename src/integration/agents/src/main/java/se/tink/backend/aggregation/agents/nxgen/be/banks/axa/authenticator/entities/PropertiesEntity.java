package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.node.ContainerNode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PropertiesEntity {

    // it changed from array to an object, as it is generally not used and we dont want to delete it
    // lets keep in as ContainerNode so it wont cause parsing exception
    private ContainerNode entry;

    @JsonObject
    public static class Entry {
        private String key;
        private String value;
    }
}
