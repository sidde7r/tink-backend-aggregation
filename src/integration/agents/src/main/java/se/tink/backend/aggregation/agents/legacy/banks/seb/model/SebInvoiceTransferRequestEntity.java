package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.seb.utilities.SEBDateUtil;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SebInvoiceTransferRequestEntity extends SebTransferRequestEntity {

    private static final String SE_BG_ID = "BG";
    private static final String SE_PG_ID = "PG";
    private static final int MSG_LINE_SIZE = 25;

    private SebInvoiceTransferRequestEntity(Transfer transfer, String customerNumber) {
        super(transfer, customerNumber);
        setDate(transfer.getDueDate());
        if (transfer.getSourceMessage() != null && transfer.getSourceMessage().length() > 20) {
            this.internalDisplayMessage = transfer.getSourceMessage().substring(0, 20);
        } else {
            this.internalDisplayMessage = transfer.getSourceMessage();
        }
    }

    public void setDate(Date dueDate) {
        this.transferDate = SEBDateUtil.getTransferDateForBgPg(dueDate);
    }

    /**
     * Only for invoices of type PG and BG, requires validation checking of OCR/Message before
     * creating
     *
     * <p>TODO: Support several message lines if needed
     */
    public static SebInvoiceTransferRequestEntity createInvoiceTransfer(
            Transfer transfer, String customerNumber) throws IllegalArgumentException {
        SebInvoiceTransferRequestEntity requestEntity =
                new SebInvoiceTransferRequestEntity(transfer, customerNumber);
        requestEntity.destinationAccountType = getAccountType(transfer.getDestination());

        if (transfer.getRemittanceInformation().isOfType(RemittanceInformationType.OCR)) {
            requestEntity.ocr = transfer.getRemittanceInformation().getValue();
        } else if (transfer.getRemittanceInformation()
                .isOfType(RemittanceInformationType.UNSTRUCTURED)) {
            List<String> messageLines =
                    Splitter.fixedLength(MSG_LINE_SIZE)
                            .splitToList(transfer.getRemittanceInformation().getValue());

            if (messageLines.size() > 4) {
                throw new IllegalStateException("Message contains of more than 100 letters");
            }

            requestEntity.messageLine1 = Iterables.get(messageLines, 0, "");
            requestEntity.messageLine2 = Iterables.get(messageLines, 1, "");
            requestEntity.messageLine3 = Iterables.get(messageLines, 2, "");
            requestEntity.messageLine4 = Iterables.get(messageLines, 3, "");
        } else {
            throw new IllegalStateException("Unknown remittance information type");
        }

        return requestEntity;
    }

    private static String getAccountType(AccountIdentifier destination) {
        if (destination.is(AccountIdentifierType.SE_PG)) {
            return SE_PG_ID;
        } else if (destination.is(AccountIdentifierType.SE_BG)) {
            return SE_BG_ID;
        }

        return null;
    }

    @JsonProperty("MOTT_KONTO_TYP")
    public String destinationAccountType;

    @JsonProperty("MOTTAGAR_INFO")
    public String ocr;

    @JsonProperty("MEDD_TXT1")
    public String messageLine1;

    @JsonProperty("MEDD_TXT2")
    public String messageLine2;

    @JsonProperty("MEDD_TXT3")
    public String messageLine3;

    @JsonProperty("MEDD_TXT4")
    public String messageLine4;

    @JsonProperty("BETAL_DATUM")
    public String transferDate;

    @JsonProperty("NOTERING")
    public String internalDisplayMessage;

    @JsonProperty("KK_TXT")
    public final String kkTxtIsEmptyForInvoice = "";
}
