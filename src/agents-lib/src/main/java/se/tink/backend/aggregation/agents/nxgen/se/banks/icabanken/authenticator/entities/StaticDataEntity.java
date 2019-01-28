package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StaticDataEntity {
    @JsonProperty("NextMonthlyDrawDate")
    private String nextMonthlyDrawDate;
    @JsonProperty("FundTradingHours")
    private FundTradingHoursEntity fundTradingHours;
    @JsonProperty("SecurityLevels")
    private List<SecurityLevelsEntity> securityLevels;

    public String getNextMonthlyDrawDate() {
        return nextMonthlyDrawDate;
    }

    public FundTradingHoursEntity getFundTradingHours() {
        return fundTradingHours;
    }

    public List<SecurityLevelsEntity> getSecurityLevels() {
        return securityLevels;
    }
}
