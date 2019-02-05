package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BeneficiaryRuleListEntity {
    @XmlElement(name = "rule")
    private List<String> rules;

    public List<String> getRules() {
        return rules;
    }

    public void setRule(List<String> rules) {
        this.rules = rules;
    }
}
