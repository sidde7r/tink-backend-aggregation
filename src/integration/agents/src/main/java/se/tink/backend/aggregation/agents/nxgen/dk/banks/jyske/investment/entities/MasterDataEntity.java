package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MasterDataEntity {
    private String id;
    private String name;
    private String nameLong;
    private String ric;
    private String isin;
    private String type;
    private String exchangeName;
    private String currency;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNameLong() {
        return nameLong;
    }

    public String getRic() {
        return ric;
    }

    public String getIsin() {
        return isin;
    }

    public String getType() {
        return type;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean hasIsin() {
        return StringUtils.isNoneBlank(isin);
    }
}
