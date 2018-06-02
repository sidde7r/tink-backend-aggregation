package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SettlementEntity {
    private String name;
    private AmountEntity balance;
    private String fullyFormattedNumber;
    private AmountEntity buyingPower;
    private AmountEntity preliminaryLiquidity;
    private LinksEntity links;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public void setBalance(AmountEntity balance) {
        this.balance = balance;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public void setFullyFormattedNumber(String fullyFormattedNumber) {
        this.fullyFormattedNumber = fullyFormattedNumber;
    }

    public AmountEntity getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(AmountEntity buyingPower) {
        this.buyingPower = buyingPower;
    }

    public AmountEntity getPreliminaryLiquidity() {
        return preliminaryLiquidity;
    }

    public void setPreliminaryLiquidity(
            AmountEntity preliminaryLiquidity) {
        this.preliminaryLiquidity = preliminaryLiquidity;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }

    public Instrument toInstrument(String rawType) {
        Instrument instrument = new Instrument();

        instrument.setCurrency(getBalance().getCurrencyCode());
        instrument.setMarketValue(getBuyingPower() != null && getBuyingPower().getAmount() == null ?
                StringUtils.parseAmount(getBuyingPower().getAmount()) : null);
        instrument.setName(getName());
        instrument.setRawType(rawType);
        instrument.setType(Instrument.Type.OTHER);
        instrument.setUniqueIdentifier(getFullyFormattedNumber());

        return instrument;
    }
}
