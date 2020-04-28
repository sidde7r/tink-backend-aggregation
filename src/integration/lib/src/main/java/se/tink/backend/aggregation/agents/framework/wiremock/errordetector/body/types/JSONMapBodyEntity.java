package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.MapComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;

public class JSONMapBodyEntity implements BodyEntity {

    private static ObjectMapper mapper = new ObjectMapper();

    private final ImmutableMap<String, Object> data;

    public JSONMapBodyEntity(String rawData) {
        try {
            this.data = ImmutableMap.copyOf(mapper.readValue(rawData, Map.class));
        } catch (IOException e) {
            throw new RuntimeException("Could not create JSONMapBodyEntity", e);
        }
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof JSONMapBodyEntity) {
            DifferenceEntity difference = null;
            difference =
                    comparor.findDifferencesInMappings(
                            this.data, ((JSONMapBodyEntity) object).data);
            return new MapComparisonReporter(difference);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "application/json";
    }
}
