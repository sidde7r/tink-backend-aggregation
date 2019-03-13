package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.SecurityCodeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityProfitabilityRequest {
    private String securityPortfolioId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date endDate;

    private SecurityCodeEntity security;

    @JsonIgnore
    public static SecurityProfitabilityRequest create(String portfolioId, String ricCode) {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.YEAR, BbvaConstants.PostParameter.START_DATE_YEAR_AGO);
        Date startDate = calendar.getTime();

        SecurityCodeEntity securityCode = SecurityCodeEntity.create(ricCode);
        SecurityProfitabilityRequest request = new SecurityProfitabilityRequest();
        request.security = securityCode;
        request.securityPortfolioId = portfolioId;
        request.startDate = startDate;
        request.endDate = endDate;

        return request;
    }
}
