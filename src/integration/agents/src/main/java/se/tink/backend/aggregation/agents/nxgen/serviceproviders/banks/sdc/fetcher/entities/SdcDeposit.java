package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import org.apache.commons.lang3.builder.ToStringBuilder;

/*
"label": "Investeringssparkonto",
"secondaryLabel": "100011",
"entityKey": SdcDepositKey,
"group": false
 */
public class SdcDeposit {
    private String label;
    private String secondaryLabel;
    private SdcDepositKey entityKey;
    private boolean group;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("label", label)
                .append("secondaryLabel", secondaryLabel)
                .append("entityKey", entityKey)
                .append("group", group)
                .toString();
    }

    public String getLabel() {
        return label;
    }

    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    public SdcDepositKey getEntityKey() {
        return entityKey;
    }

    public boolean isGroup() {
        return group;
    }
}
