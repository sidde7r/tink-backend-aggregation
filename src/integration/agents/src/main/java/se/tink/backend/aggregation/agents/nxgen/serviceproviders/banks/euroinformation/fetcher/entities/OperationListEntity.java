package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "operations_list")
public class OperationListEntity {
    @XmlElement(name = "recoverykey")
    private String recoveryKey;

    @XmlElement(name = "nb_operations_total")
    private String totalNumberOfOperations;

    @XmlElement(name = "nb_operations_page")
    private String numberOfReturnedOperations;

    @XmlElement(name = "operation")
    @XmlElementWrapper(name = "operations")
    private List<OperationEntity> transactions;

    public String getNumberOfReturnedOperations() {
        return numberOfReturnedOperations;
    }

    public String getRecoveryKey() {
        return recoveryKey;
    }

    public String getTotalNumberOfOperations() {

        return totalNumberOfOperations;
    }

    public List<OperationEntity> getTransactions() {
        return transactions;
    }
}
