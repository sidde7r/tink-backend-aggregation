package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.XmlParser;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.PlainTextComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;

public class XMLBodyEntity implements BodyEntity {

    private static final XmlParser parser = new XmlParser();

    private final String data;

    public XMLBodyEntity(String rawData) {
        this.data = parser.normalizeXmlData(rawData);
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof XMLBodyEntity) {
            return new PlainTextComparisonReporter(data, ((XMLBodyEntity) object).data);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "text/xml";
    }
}
