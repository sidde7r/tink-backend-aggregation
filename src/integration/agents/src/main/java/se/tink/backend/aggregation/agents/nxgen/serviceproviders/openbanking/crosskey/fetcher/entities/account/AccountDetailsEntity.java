package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountDetailsEntity {

    private String identification;
    private String name;
    private String schemeName;
    private String secondaryIdentification;

    public String getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public boolean isIBAN(){
        return StringUtils.containsIgnoreCase(schemeName, "iban");
    }
}
