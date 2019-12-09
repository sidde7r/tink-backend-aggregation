package se.tink.backend.aggregation.agents.standalone.mapper.providers.impl;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.standalone.mapper.providers.CommonExternalParametersProvider;

public class MockCommonExternalParametersProvider implements CommonExternalParametersProvider {

    private static final String BANK_CODE = "BANK_CODE";

    @Override
    public Map<String, String> buildExternalParametersMap() {

        Map<String, String> map = new HashMap<>();
        map.put(BANK_CODE, "BCPPT");

        return map;
    }
}
