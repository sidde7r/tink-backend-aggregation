package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction.CreditorAccount;

@Getter
@Setter
public class BookedEntity {

    @JsonFormat(pattern = SibsConstants.Formats.TRANSACTION_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = SibsConstants.Formats.TRANSACTION_DATE_FORMAT)
    private Date valueDate;

    private AmountEntity amount;
    private CreditorAccount creditorAccount;
    private String remittanceInformationUnstructured;
}
