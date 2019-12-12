package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AbstractContractDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class BaseLoanEntity extends AbstractContractDetailsEntity {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date nextPaymentDate;

    private AmountEntity nextFee;

    private AmountEntity redeemedBalance;
    private AmountEntity finalFee;

    @JsonIgnore
    protected IdModule getIdModuleWithUniqueIdentifier(String uniqueIdentifier) {
        return IdModule.builder()
                .withUniqueIdentifier(uniqueIdentifier)
                .withAccountNumber(uniqueIdentifier)
                .withAccountName(getProduct().getName())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.BBAN, getFormats().getBocf()))
                .setProductName(getProduct().getName())
                .build();
    }
}
