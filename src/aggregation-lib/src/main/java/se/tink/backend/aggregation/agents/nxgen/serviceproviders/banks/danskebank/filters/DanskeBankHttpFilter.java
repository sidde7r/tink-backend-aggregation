package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters;

import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public abstract class DanskeBankHttpFilter extends Filter {
    protected static final String CLIENT_ID_KEY = "x-ibm-client-id";
    protected static final String CLIENT_ID_VALUE = "5ec4b8ad-a93d-43e1-831c-8e78ee6e661a";
    protected static final String CLIENT_SECRET_KEY = "x-ibm-client-secret";
    protected static final String CLIENT_SECRET_VALUE = "lJFXVTCZ3nAVyPXfsZ7aXzwsD41WdYT2y4rxICt5mSEe9xMgPh";
    protected static final String APP_CULTURE_KEY = "x-app-culture";
    protected static final String ADRUM_KEY = "ADRUM";
    protected static final String ADRUM_VALUE = "isAjax:true";
    protected static final String APP_VERSION_KEY = "x-app-version";
    protected static final String ADRUM1_KEY = "ADRUM_1";
    protected static final String ADRUM1_VALUE = "isMobile:true";
}
