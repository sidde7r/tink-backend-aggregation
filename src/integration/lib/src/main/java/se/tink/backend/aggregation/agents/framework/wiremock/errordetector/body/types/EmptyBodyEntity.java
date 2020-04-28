package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.NoDifferenceComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;

public class EmptyBodyEntity implements BodyEntity {

    public EmptyBodyEntity() {}

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof EmptyBodyEntity) {
            return new NoDifferenceComparisonReporter();
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "Empty Body";
    }
}
