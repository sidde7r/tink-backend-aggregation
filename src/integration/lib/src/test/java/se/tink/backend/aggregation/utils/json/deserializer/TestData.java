package se.tink.backend.aggregation.utils.json.deserializer;

public class TestData {

    public static final String VALID_JSON =
            "{\n"
                    + "    \"TestEntities\": [\n"
                    + "        {\n"
                    + "            \"name\": \"adam\",\n"
                    + "            \"data\": \"oranges\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"name\": \"steve\",\n"
                    + "            \"data\": \"thoughts\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    public static final String MISSING_NAME_ATTR =
            "{\n"
                    + "    \"TestEntities\": [\n"
                    + "        {\n"
                    + "            \"data\": \"oranges\"\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"data\": \"thoughts\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    public static final String NOT_AN_ARRAY =
            "{\n"
                    + "    \"TestEntities\": {\n"
                    + "        \"data\": \"oranges\"\n"
                    + "    }\n"
                    + "}";

    public static final String NAME_IS_NOT_STRING =
            "{\n"
                    + "    \"TestEntities\": [\n"
                    + "        {\n"
                    + "            \"name\": {\n"
                    + "                \"value\": \"adam\"\n"
                    + "            },\n"
                    + "            \"data\": \"oranges\"\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
}
