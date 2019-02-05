package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class TransactionEntity {
    private int seqNr;
    private int startIndex;
    private int endIndex;
    private String amount;
    private String postingDate;
    private String valueDate;
    private String balance;
    private String bbanNumber;
    private String categoryCode;
    private String category;
    private String subCategoryCode;
    private String subCategory;
    private List<String> details;
    private List<String> extraDetails;
    private String number;
    private String posId;
    private String counterpartyAccountNr;
    private String dayCode;
    private String sequence;

    public int getSeqNr() {
        return seqNr;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getAmount() {
        return amount;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getBalance() {
        return balance;
    }

    public String getBbanNumber() {
        return bbanNumber;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategory() {
        return category;
    }

    public String getSubCategoryCode() {
        return subCategoryCode;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public List<String> getDetails() {
        return details != null ? details : Collections.emptyList();
    }

    public List<String> getExtraDetails() {
        return extraDetails != null ? extraDetails : Collections.emptyList();
    }

    public String getNumber() {
        return number;
    }

    public String getPosId() {
        return posId;
    }

    public String getCounterpartyAccountNr() {
        return counterpartyAccountNr;
    }

    public String getDayCode() {
        return dayCode;
    }

    public String getSequence() {
        return sequence;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getTinkDescription())
                .setAmount(Amount.inEUR(IngHelper.parseAmountStringToDouble(amount)))
                .setDate(DateUtils.parseDate(postingDate))
                .setRawDetails(getRawDetails())
                .build();
    }

    // for Fortis we need to expose the entire transaction details
    @JsonIgnore
    private RawDetails getRawDetails() {
        if ((details == null || details.isEmpty()) && (extraDetails == null || extraDetails.isEmpty())) {
            return null;
        }

        return new RawDetails(details, extraDetails);
    }

    // There are two fields that contain transaction details: details and extraDetails. Each field is a list of
    // strings. Return null if both fields are empty. The second element in details is the most descriptive, if
    // that's not present try to take the first. If details is empty return first element in extraDetails.
    @JsonIgnore
    private String getTinkDescription() {
        String descriptionText = getDescriptionText();

        Matcher matcher = IngConstants.Transactions.TRANSACTION_PREFIX_PATTERN.matcher(descriptionText);
        if (matcher.matches() && matcher.groupCount() > 1) {
            return descriptionText.replace(matcher.group(1), "").trim();
        }

        return descriptionText;
    }

    @JsonIgnore
    private String getDescriptionText() {
        if (details.isEmpty() && extraDetails.isEmpty()) {
            return null;
        }

        int detailsSize = details.size();

        if (detailsSize > 0) {
            if (detailsSize > 1) {
                return details.get(1);
            }
            return details.get(0);
        }

        return extraDetails.get(0);
    }

    @JsonObject
    public class RawDetails {
        private List<String> details;
        private List<String> extraDetails;

        public RawDetails(List<String> details, List<String> extraDetails) {
            // be kind no nulls
            this.details = details != null ? details : Collections.emptyList();
            this.extraDetails = extraDetails != null ? extraDetails : Collections.emptyList();
        }
    }
}
