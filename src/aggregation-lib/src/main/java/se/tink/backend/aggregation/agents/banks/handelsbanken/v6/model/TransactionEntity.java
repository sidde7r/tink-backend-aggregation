package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.PendingStringTypes;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.SHBUtils;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
    private AmountEntity amount;
    private String dueDate;
    private List<LinkEntity> links;
    private RecipientAccountEntity recipient;

    public AmountEntity getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public RecipientAccountEntity getRecipient() {
        return recipient;
    }

    public void setAmount(AmountEntity amount) {
        this.amount = amount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setRecipient(RecipientAccountEntity recipient) {
        this.recipient = recipient;
    }

    public Transaction toTransaction() {
        return toTransaction(false);
    }

    public Transaction toTransaction(boolean pendingPayment) {
        Transaction transaction = new Transaction();

        String rawDescription = pendingPayment ? recipient.getName() : recipient.getAdditionalInfo();
        double rawAmount = StringUtils.parseAmount(amount.getAmountFormatted());

        transaction.setDescription(SHBUtils.unescapeAndCleanTransactionDescription(rawDescription));
        transaction.setAmount(pendingPayment ? rawAmount * -1 : rawAmount);
        transaction.setDate(DateUtils.flattenTime(DateUtils.parseDate(dueDate)));

        String lowerCaseDescription = transaction.getDescription().toLowerCase();
        if (lowerCaseDescription.startsWith("prel ") || lowerCaseDescription.startsWith("prel.")) {
            transaction.setDescription(
                    SHBUtils.unescapeAndCleanTransactionDescription(transaction.getDescription().substring(5)));
            transaction.setPending(true);
        } else if (transaction.getDescription().equalsIgnoreCase(PendingStringTypes.HANDELSBANKEN.getValue())
                || lowerCaseDescription.equals("prel") || pendingPayment) {
            transaction.setPending(true);
        }

        return transaction;
    }

    public List<LinkEntity> getLinks() {
        return links;
    }

    public void setLinks(List<LinkEntity> links) {
        this.links = links;
    }

    public boolean isAbandonedOrSuspended() {
        if (links == null) {
            return false;
        }

        for (LinkEntity link : links) {
            if (link != null && link.getHref() != null) {
                String lowerCaseLink = link.getHref().toLowerCase();
                return lowerCaseLink.contains("status=suspended") || lowerCaseLink.contains("status=abandoned");
            }
        }
        return false;
    }
}
