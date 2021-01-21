package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import java.util.Date;
import java.util.Optional;

public abstract class AbstractContractDetailsEntity {

    private String country;
    private FormatsEntity formats;
    private MarketerBankEntity marketerBank;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

    private BranchEntity branch;
    private BankEntity bank;
    private CurrencyEntity currency;
    private String id;
    private List<ParticipantEntity> participants;
    private ProductEntity product;
    private String counterPart;
    private JoinTypeEntity joinType;
    private String sublevel;
    private UserCustomizationEntity userCustomization;

    public String getCountry() {
        return country;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public MarketerBankEntity getMarketerBank() {
        return marketerBank;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public BranchEntity getBranch() {
        return branch;
    }

    public BankEntity getBank() {
        return bank;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public List<ParticipantEntity> getParticipants() {
        return participants;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public JoinTypeEntity getJoinType() {
        return joinType;
    }

    public String getSublevel() {
        return sublevel;
    }

    public UserCustomizationEntity getUserCustomization() {
        return userCustomization;
    }

    protected abstract String getAccountNumber();

    @JsonIgnore
    protected String getAccountName() {
        return Optional.ofNullable(userCustomization)
                .map(UserCustomizationEntity::getAlias)
                .orElse(getDefaultAccountName());
    }

    @JsonIgnore
    private String getDefaultAccountName() {
        String accountNumber = getAccountNumber();
        Optional<String> last4AccountNumber =
                Optional.ofNullable(accountNumber)
                        .filter(pan -> pan.length() >= 4)
                        .map(pan -> pan.substring(pan.length() - 4));

        return last4AccountNumber
                .map(last4 -> product.getName() + " *" + last4)
                .orElse(accountNumber);
    }
}
