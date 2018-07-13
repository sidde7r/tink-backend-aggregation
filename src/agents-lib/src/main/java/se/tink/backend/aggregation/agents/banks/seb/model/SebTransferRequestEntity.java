package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Locale;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.banks.seb.SebAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.backend.core.transfer.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SebTransferRequestEntity implements MatchableTransferRequestEntity {
    private static final AccountIdentifierFormatter SEB_ACCOUNT_IDENTIFIER_FORMATTER = new SebAccountIdentifierFormatter();

    public SebTransferRequestEntity() {}

    SebTransferRequestEntity(Transfer transfer, String customerNumber) {
        Amount = formatAmmountWith2Decimals(transfer);
        CustomerNumber = customerNumber;
        DestinationAccount = transfer.getDestination().getIdentifier(SEB_ACCOUNT_IDENTIFIER_FORMATTER);
        SourceAccount = transfer.getSource().getIdentifier(new DefaultAccountIdentifierFormatter());
    }

    private static String formatAmmountWith2Decimals(Transfer transfer) {
        return String.format(Locale.ENGLISH, "%.2f", transfer.getAmount().getValue());
    }

    @JsonProperty("UPPDRAG_BEL")
    public String Amount;

    @JsonProperty("SEB_KUND_NR")
    public String CustomerNumber;

    @JsonProperty("MOTT_KONTO_NR")
    public String DestinationAccount;

    @JsonProperty("KONTO_NR")
    public String SourceAccount;

    @JsonProperty("UPPDRAG_BESKRIV")
    public final String AssignmentDescription = ""; // not used

    @JsonProperty("MOTTAGAR_REG_FL")
    public final String MOTTAGAR_REG_FL = ""; // not used

    @Override
    @JsonIgnore
    public boolean matches(TransferListEntity transferListEntity) {
        // Source and destination accounts cannot be null
        if (this.DestinationAccount == null || this.SourceAccount == null ||
                transferListEntity.SourceAccountNumber == null || transferListEntity.DestinationAccountNumber == null) {
            return false;
        }

        // Accounts and the amount must match
        return
                SEBAgentUtils.trimmedDashAgnosticEquals(this.DestinationAccount, transferListEntity.DestinationAccountNumber) &&
                SEBAgentUtils.trimmedDashAgnosticEquals(this.SourceAccount, transferListEntity.SourceAccountNumber) &&
                Math.abs(Double.parseDouble(this.Amount) - transferListEntity.Amount) < 0.01;
    }
}
