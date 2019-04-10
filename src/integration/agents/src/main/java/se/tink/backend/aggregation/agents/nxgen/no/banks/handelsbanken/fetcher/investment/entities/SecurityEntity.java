package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityEntity {
    private String securityName20;
    private String securityType;
    private String securityGroup;
    private String securityTicker;
    private String securityName;
    private String uri;
    private String securityName34;
    private String isin;

    public String getSecurityName20() {
        return securityName20;
    }

    public String getSecurityType() {
        return securityType;
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public String getSecurityTicker() {
        return Strings.isNullOrEmpty(securityTicker) ? null : securityTicker;
    }

    public String getSecurityName() {
        return securityName;
    }

    public String getUri() {
        return uri;
    }

    public String getSecurityName34() {
        return securityName34;
    }

    public String getIsin() {
        return isin;
    }
}
