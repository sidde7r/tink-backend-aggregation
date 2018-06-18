package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities.SecurityAccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankMsgDateDeserializer;

@XmlRootElement(name = "root")
public class InvestmentAccountsListResponse {
    @XmlElement(name = "code_retour")
    private String returnCode;
    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(TargoBankMsgDateDeserializer.class)
    private Date date;
    @XmlElementWrapper(name = "SecurityAccountList")
    @XmlElement(name = "SecurityAccount")
    private List<SecurityAccountListEntity> securityAccountList;

    public String getReturnCode() {
        return returnCode;
    }

    public Date getDate() {
        return date;
    }

    public List<SecurityAccountListEntity> getSecurityAccountList() {
        return securityAccountList;
    }
}
