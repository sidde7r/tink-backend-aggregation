package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CategoryEntity {
    private String number;
    private String label;
    private String totalBalance;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(String totalBalance) {
        this.totalBalance = totalBalance;
    }
}
