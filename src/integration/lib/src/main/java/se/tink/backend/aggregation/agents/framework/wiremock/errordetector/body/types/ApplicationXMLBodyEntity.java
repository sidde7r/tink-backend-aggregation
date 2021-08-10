package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

public class ApplicationXMLBodyEntity extends XMLBodyEntity {
    public ApplicationXMLBodyEntity(String rawData) {
        super(rawData);
    }

    @Override
    public String getBodyType() {
        return "application/xml";
    }
}
