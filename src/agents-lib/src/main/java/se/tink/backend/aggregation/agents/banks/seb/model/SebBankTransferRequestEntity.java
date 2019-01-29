package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.seb.utilities.SEBDateUtil;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SebBankTransferRequestEntity extends SebTransferRequestEntity {
    private SebBankTransferRequestEntity(Transfer transfer, String customerNumber, boolean withinSEB) {
        super(transfer, customerNumber);
        transferDate = SEBDateUtil.nextPossibleTransferDate(new Date(), withinSEB);
    }

    /**
     * "Internal transfer" means between the same logged in user.
     *
     * SEB default populates messages in a weird way, only using the internal KK_TXT for messgae,
     * so we don't use the message formatter on these if no message is set on transfer object
     */
    public static SebBankTransferRequestEntity createInternalBankTransfer(Transfer transfer, String customerNumber,
            TransferMessageFormatter transferMessageFormatter) throws TransferMessageException {
        SebBankTransferRequestEntity request = new SebBankTransferRequestEntity(transfer, customerNumber, true);
        request.type = "E";
        request.bankPrefix = "SEB";

        if (!Strings.isNullOrEmpty(transfer.getDestinationMessage())) {
            TransferMessageFormatter.Messages messages = transferMessageFormatter.getMessages(transfer, true);
            request.destinationMessage = messages.getDestinationMessage();
            request.sourceMessage = messages.getDestinationMessage();
            return request;
        } else {
            return request;
        }
    }

    /**
     * See #createInternalTransfer for what this is not.
     */
    public static SebBankTransferRequestEntity createExternalBankTransfer(Transfer transfer, String customerNumber,
            TransferMessageFormatter transferMessageFormatter) throws TransferMessageException {

        boolean withinSEB = false;
        if (Objects.equal(transfer.getDestination().getType(), AccountIdentifier.Type.SE)) {
            Optional<ClearingNumber.Details> clearingNumber = ClearingNumber.get(
                    transfer.getDestination().to(SwedishIdentifier.class)
                            .getClearingNumber());
            withinSEB = clearingNumber.isPresent() &&
                    Objects.equal(clearingNumber.get().getBank(), ClearingNumber.Bank.SEB);
        }

        SebBankTransferRequestEntity request = new SebBankTransferRequestEntity(transfer, customerNumber, withinSEB);
        request.type = "M";
        request.bankPrefix = BankPrefix.fromAccountIdentifier(transfer.getDestination());

        TransferMessageFormatter.Messages messages = transferMessageFormatter.getMessages(transfer, false);
        request.destinationMessage = messages.getDestinationMessage();
        request.sourceMessage = messages.getSourceMessage();

        return request;
    }

    @JsonProperty("OVERF_DAT")
    public String transferDate;

    @JsonProperty("OVERF_K_TYP")
    public String type;

    @JsonProperty("BANK_PREFIX")
    public String bankPrefix;

    @JsonProperty("KK_TXT")
    public String sourceMessage;

    @JsonProperty("KK_TXT_MOT")
    public String destinationMessage;

    @JsonProperty("AVI_KOD")
    public final String AVI_KOD = "N";

    @JsonProperty("AVI_TXT")
    public final String AVI_TXT = ""; // not used

    @JsonProperty("KORT_NAMN")
    public final String KORT_NAMN = ""; // not used
}
