package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.SecurityAccountOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankMsgDateDeserializer;

@XmlRootElement(name = "root")
public class InvestmentAccountOverviewResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;
    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(TargoBankMsgDateDeserializer.class)
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
