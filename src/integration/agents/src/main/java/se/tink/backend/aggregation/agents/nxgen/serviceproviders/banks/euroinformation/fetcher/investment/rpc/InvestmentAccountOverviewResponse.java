package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.SecurityAccountOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "root")
public class InvestmentAccountOverviewResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    private Date date;

    @XmlElement(name = "SecurityAccountOverview")
    private SecurityAccountOverviewEntity securityAccountList;

    public String getReturnCode() {
        return returnCode;
    }

    public Date getDate() {
        return date;
    }

    public SecurityAccountOverviewEntity getSecurityAccountOverview() {
        return securityAccountList;
    }
}
