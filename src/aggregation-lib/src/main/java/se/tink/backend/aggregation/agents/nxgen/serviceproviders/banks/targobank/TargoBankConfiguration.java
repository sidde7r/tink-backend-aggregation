package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import se.tink.backend.aggregation.nxgen.http.HeaderEnum;

public interface TargoBankConfiguration {

    public HeaderEnum getHost();

    public String getUrl();

}
