package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationMsgDateDeserializer;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionSummaryResponse {

    @XmlElement(name = "code_retour")
    private String returnCode;

    @XmlElement(name = "date_msg")
    @XmlJavaTypeAdapter(EuroInformationMsgDateDeserializer.class)
    private Date date;

    @XmlElement(name = "ligmvt")
    @XmlElementWrapper(name = "tabmvt")
    private List<TransactionEntity> transactions;

    public String getReturnCode() {
        return returnCode;
    }

    public Date getDate() {
        return date;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }
}
