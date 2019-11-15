package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BranchEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.CurrencyEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FormatsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.JoinTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.MarketerBankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ParticipantEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserCustomizationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class BaseLoanEntity {
    private String country;
    private ProductEntity product;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date nextPaymentDate;

    private FormatsEntity formats;
    private AmountEntity nextFee;
    private String counterPart;
    private MarketerBankEntity marketerBank;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

    private BranchEntity branch;
    private AmountEntity redeemedBalance;
    private BankEntity bank;
    private AmountEntity finalFee;
    private JoinTypeEntity joinType;
    private String sublevel;
    private CurrencyEntity currency;
    private String id;
    private UserCustomizationEntity userCustomization;
    private List<ParticipantEntity> participants;

    public String getId() {
        return id;
    }

    @JsonIgnore
    protected IdModule getIdModuleWithUniqueIdentifier(String uniqueIdentifier) {
        return IdModule.builder()
                .withUniqueIdentifier(uniqueIdentifier)
                .withAccountNumber(uniqueIdentifier)
                .withAccountName(product.getName())
                .addIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.BBAN, formats.getBocf()))
                .setProductName(product.getName())
                .build();
    }
}
