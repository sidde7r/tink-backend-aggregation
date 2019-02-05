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

@XmlRootElement(name = "operations")
@XmlAccessorType(XmlAccessType.FIELD)
public class OperationEntity {
    @XmlElement(name = "dat")
    @XmlJavaTypeAdapter(EuroInformationDateDeserializer.class)
    private Date date;

    @XmlElement(name = "mnt")
    private String value;
    @XmlElement(name = "lib")
    private String transactionDescription;
    @XmlElement(name = "dat_ori")
    @XmlJavaTypeAdapter(EuroInformationDateDeserializer.class)
    private Date dateOriginal;
    @XmlElement(name = "webid")
    private String webId;
    @XmlElement(name = "cat")
    private String category;
    @XmlElement(name = "car_ori")
    private String originalCategory;
    @XmlElement(name = "typmvt")
    private String typeOfMovement;
    @XmlElement(name = "rop")
    private String rop;
    @XmlElement(name = "usr_new")
    private String userNewTransactions;
    @XmlElement(name = "usr_chk")
    private String userChkTransactions;

    public Date getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public Transaction toTransaction() {
        return Transaction.builder()
                .setDate(date)
                .setAmount(EuroInformationUtils.parseAmount(value))
                .setDescription(getTransactionDescription()).build();
    }
}
