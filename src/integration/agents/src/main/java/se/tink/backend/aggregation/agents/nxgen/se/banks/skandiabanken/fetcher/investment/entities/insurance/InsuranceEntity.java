package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.HolderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class InsuranceEntity {
    @JsonProperty("Holder")
    private HolderEntity holder;

    @JsonProperty("IsAiEInsurance")
    private boolean isAiEInsurance;

    @JsonProperty("HasSecuritiesAccountPart")
    private boolean hasSecuritiesAccountPart;

    @JsonProperty("Parts")
    private List<InsurancePartEntity> parts;

    @JsonProperty("Category1")
    private String category1;

    @JsonProperty("Category2")
    private String category2;

    @JsonProperty("InsuranceCategory")
    private int insuranceCategory;

    @JsonProperty("InsuranceCategoryName")
    private String insuranceCategoryName;

    @JsonProperty("Type")
    private int type;

    @JsonProperty("DisplayTypeName")
    private String displayTypeName;

    @JsonProperty("DisplayManagement")
    private String displayManagement;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    @JsonProperty("TypeName")
    private String typeName;

    @JsonIgnore
    private HolderName getHolderName() {
        return parts.stream()
                .map(InsurancePartEntity::getHolder)
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
    public List<InsurancePartEntity> getParts() {
        return parts;
    }

    @JsonIgnore
    public boolean hasSecuritiesAccountPart() {
        return hasSecuritiesAccountPart;
    }

    public Optional<SecuritiesAccountsEntity> getSecuritiesAccountPart() {
        return parts.stream()
                .filter(InsurancePartEntity::isSecuritiesAccount)
                .findFirst()
                .map(InsurancePartEntity::getSecuritiesAccount);
    }
}
