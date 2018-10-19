package se.tink.backend.aggregation.utils.json.deserializer;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TestResultEntity {

    public TestResultEntity() {

    }

    public TestResultEntity(String name, String data) {
        this.name = name;
        this.data = data;
    }

    private String name;
    private String data;

    @Override
    public boolean equals(Object o) {
        if (o instanceof TestResultEntity) {
            TestResultEntity other = (TestResultEntity) o;

            return other.data.equals(data)
                    && other.name.equals(name);
        }
        return false;
    }
}
