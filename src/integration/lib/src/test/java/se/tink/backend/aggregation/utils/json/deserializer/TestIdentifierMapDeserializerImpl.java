package se.tink.backend.aggregation.utils.json.deserializer;

import se.tink.backend.aggregation.utils.json.deserializers.IdentifierMapDeserializer;

public class TestIdentifierMapDeserializerImpl
        extends IdentifierMapDeserializer<String, TestResultEntity> {

    private static final String KEY_ATTRIBUTE = "name";

    public TestIdentifierMapDeserializerImpl() {
        super(KEY_ATTRIBUTE, String.class, TestResultEntity.class);
    }
}
