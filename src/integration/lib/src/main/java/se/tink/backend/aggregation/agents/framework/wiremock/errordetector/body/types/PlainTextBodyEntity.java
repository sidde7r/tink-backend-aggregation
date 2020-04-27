package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.PlainTextComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;

public class PlainTextBodyEntity implements BodyEntity {

    private final String data;

    public PlainTextBodyEntity(String rawData) {
        this.data = rawData;
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof PlainTextBodyEntity) {
            return new PlainTextComparisonReporter(data, ((PlainTextBodyEntity) object).data);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "text/plain";
    }
}
