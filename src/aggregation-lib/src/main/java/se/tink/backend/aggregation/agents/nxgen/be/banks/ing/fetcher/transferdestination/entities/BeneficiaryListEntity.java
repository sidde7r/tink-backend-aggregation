package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.entities;

import java.util.List;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BeneficiaryListEntity {

    @XmlElement(name = "beneficiary")
    private List<BeneficiaryEntity> beneficiaryList;

    public List<BeneficiaryEntity> getBeneficiaryList() {
        return beneficiaryList;
    }

    public void setBeneficiary(List<BeneficiaryEntity> beneficiaryList) {
        this.beneficiaryList = beneficiaryList;
    }

    public Stream<BeneficiaryEntity> stream() {
        return beneficiaryList != null ? beneficiaryList.stream() : Stream.empty();
    }
}
