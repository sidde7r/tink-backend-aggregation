package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundCompanyEntity {
    private String name;
    private String homePage;

    public String getName() {
        return name;
    }

    public String getHomePage() {
        return homePage;
    }
}
