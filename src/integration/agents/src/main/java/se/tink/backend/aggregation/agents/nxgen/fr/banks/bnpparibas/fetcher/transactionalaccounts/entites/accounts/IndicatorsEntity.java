package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndicatorsEntity {
    @JsonProperty("indicAgenceNet")
    private boolean isAgencyNet;
    @JsonProperty("indicAgentBNP")
    private String agencyBnpIndicator;
    @JsonProperty("indicBanquePrive")
    private boolean isPrivateBank;
    private int indicDematerialisation;
    @JsonProperty("indicTypeBPF")
    private int bpfType;
    private boolean indicPriority;
    private boolean indicTypePro;
    @JsonProperty("indicTypeHB")
    private boolean indicTypehb;
    @JsonProperty("indicAgregation")
    private boolean hasAggergation;
    @JsonProperty("indicVirInternational")
    private boolean allowsInternationalTransfers;

    public boolean isAgencyNet() {
        return isAgencyNet;
    }

    public String getAgencyBnpIndicator() {
        return agencyBnpIndicator;
    }

    public boolean isPrivateBank() {
        return isPrivateBank;
    }

    public int getIndicDematerialisation() {
        return indicDematerialisation;
    }

    public int getBpfType() {
        return bpfType;
    }

    public boolean isIndicPriority() {
        return indicPriority;
    }

    public boolean isIndicTypePro() {
        return indicTypePro;
    }

    public boolean isIndicTypehb() {
        return indicTypehb;
    }

    public boolean isHasAggergation() {
        return hasAggergation;
    }

    public boolean isAllowsInternationalTransfers() {
        return allowsInternationalTransfers;
    }
}
