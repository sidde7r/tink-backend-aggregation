package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Portfolio.Type;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.PensionFundsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class PensionEntity {

    @JsonProperty("Category1")
    private String category1;

    @JsonProperty("Category2")
    private String category2;

    @JsonProperty("DisplayManagement")
    private String displayManagement;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("DisplayTypeName")
    private String displayTypeName;

    @JsonProperty("HasSecuritiesAccountPart")
    private boolean hasSecuritiesAccountPart;

    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("InsuranceCategory")
    private int insuranceCategory;

    @JsonProperty("InsuranceCategoryName")
    private String insuranceCategoryName;

    @JsonProperty("IsAiEInsurance")
    private boolean isAiEInsurance;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("Parts")
    private List<PartsEntity> parts;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("TypeName")
    private String typeName;

    public List<PartsEntity> getParts() {
        return parts;
    }

    public void setParts(List<PartsEntity> parts) {
        this.parts = parts;
    }

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount() {
        return InvestmentAccount.builder(this.number.replaceAll("[^\\d]", ""))
                .setAccountNumber(this.number)
                .setName(this.displayName)
                .setHolderName(getHolderName())
                .setPortfolios(
                        this.parts.stream()
                                .map(this::getTinkPortfolio)
                                .collect(Collectors.toList()))
                .setCashBalance(Amount.inSEK(0.0)) // Amount is set in framework from parts.
                .build();
    }

    @JsonIgnore
    private HolderName getHolderName() {
        return parts.stream()
                .map(PartsEntity::getHolder)
                .filter(Objects::nonNull)
                .map(HolderEntity::getFullName)
                .findFirst()
                .map(HolderName::new)
                .orElse(
                        Optional.ofNullable(holder)
                                .map(HolderEntity::getFullName)
                                .map(HolderName::new)
                                .orElse(new HolderName(null)));
    }

    @JsonIgnore
    private Portfolio getTinkPortfolio(PartsEntity part) {
        final Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(part.getNumber());
        portfolio.setRawType(part.getTypeName());
        portfolio.setType(Type.PENSION);
        portfolio.setTotalValue(part.getValue());
        portfolio.setInstruments(getInstrumentsList(part));
        return portfolio;
    }

    @JsonIgnore
    private List<Instrument> getInstrumentsList(PartsEntity part) {
        return Optional.ofNullable(getFunds(part)).orElse(Collections.emptyList()).stream()
                .map(this::toTinkPensionInstrument)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private List<FundsEntity> getFunds(PartsEntity part) {
        return Optional.ofNullable(part.getPensionFunds())
                .orElse(new PensionFundsResponse())
                .getFunds();
    }

    @JsonIgnore
    private Instrument toTinkPensionInstrument(FundsEntity fund) {
        final Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(fund.getId());
        instrument.setIsin(fund.getId());
        instrument.setCurrency(fund.getCurrency());
        instrument.setType(Instrument.Type.FUND);
        instrument.setQuantity(fund.getShares());
        instrument.setPrice(fund.getRate());
        instrument.setName(fund.getName());
        instrument.setMarketValue(fund.getMarketValue());
        return instrument;
    }
}
