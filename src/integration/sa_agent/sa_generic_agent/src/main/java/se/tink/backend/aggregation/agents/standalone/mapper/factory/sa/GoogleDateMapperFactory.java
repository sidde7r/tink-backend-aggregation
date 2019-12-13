package se.tink.backend.aggregation.agents.standalone.mapper.factory.sa;

import se.tink.backend.aggregation.agents.standalone.mapper.common.GoogleDateMapper;

public class GoogleDateMapperFactory {

    private GoogleDateMapperFactory() {}

    public static GoogleDateMapperFactory newInstance() {
        return new GoogleDateMapperFactory();
    }

    public GoogleDateMapper fetchGoogleDateMapper() {
        return new GoogleDateMapper();
    }
}
