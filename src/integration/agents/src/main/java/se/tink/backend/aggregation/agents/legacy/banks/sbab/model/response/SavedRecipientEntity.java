package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class SavedRecipientEntity implements GeneralAccountEntity {

    private static final AggregationLogger log = new AggregationLogger(SavedRecipientEntity.class);

    // TODO: validate account number lengths?
    private static final Pattern pattern = Pattern.compile("[\\d]*[|]{1}[A-z]*[|]{1}[^|]*");

    private String name;
    private String accountNumber;
    private String label;
    private boolean isUserAccount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isUserAccount() {
        return isUserAccount;
    }

    public void setIsUserAccount(boolean isUserAccount) {
        this.isUserAccount = isUserAccount;
    }

    /** The recipient String should be on the format "12345|BI|ExampleName". */
    public static Optional<SavedRecipientEntity> createFromString(String recipientString) {
        SavedRecipientEntity recipientEntity = new SavedRecipientEntity();

        Matcher matcher = pattern.matcher(recipientString);

        if (!matcher.matches()) {
            log.error(
                    "The given recipient string (" + recipientString + ") has an unknown format.");
            return Optional.empty();
        }

        String[] recipientStringParts = recipientString.split(("\\|"));
        String accountNumber = recipientStringParts[0];

        // SBAB has account numbers with only numbers in them and no additional characters.
        if (!accountNumber.matches("\\d+")) {
            log.error(
                    "The given recipient string ("
                            + recipientString
                            + ") contains an account number with an unknown format.");
            return Optional.empty();
        }

        recipientEntity.setAccountNumber(accountNumber);

        String recipientLabel = recipientStringParts[1];
        recipientEntity.setLabel(recipientLabel);

        String name = recipientStringParts[2];
        recipientEntity.setName(name);

        return Optional.of(recipientEntity);
    }

    /**
     * New recipients are formatted in the same way as existing ones, except for the fields being
     * replaced by "-" characters.
     */
    public static Optional<String> getNewRecipientIdentifier(String accountNumber) {
        SavedRecipientEntity savedRecipientEntity = new SavedRecipientEntity();
        savedRecipientEntity.setAccountNumber(accountNumber);
        savedRecipientEntity.setLabel("-");
        savedRecipientEntity.setName("-");
        return savedRecipientEntity.getSBABIdentifier();
    }

    /** Used at SBAB to identify an account. */
    public Optional<String> getSBABIdentifier() {
        if (getAccountNumber() != null && getLabel() != null && getName() != null) {
            return Optional.of(getAccountNumber() + "|" + getLabel() + "|" + getName());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getAccountNumber());
    }

    @Override
    public String generalGetBank() {
        if (generalGetAccountIdentifier().isValid()) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return getName();
    }
}
