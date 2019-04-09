package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationDateDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@XmlRootElement(name = "ligmvt")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionEntity {

    @XmlElement(name = "dat")
    @XmlJavaTypeAdapter(EuroInformationDateDeserializer.class)
    private Date date;

    @XmlElement(name = "lib")
    private String transactionDescription;

    @XmlElement(name = "mnt")
    private String value;

    public Date getDate() {
        return date;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public String getValue() {
        return value;
    }

    public Transaction toTransaction() {
        return Transaction.builder()
                .setDate(getDate())
                .setAmount(EuroInformationUtils.parseAmount(value))
                .setDescription(getTransactionDescription())
                .build();
    }
}
