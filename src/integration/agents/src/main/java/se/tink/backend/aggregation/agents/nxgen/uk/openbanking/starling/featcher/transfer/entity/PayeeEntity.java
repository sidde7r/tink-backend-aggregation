package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class PayeeEntity {

    private String payeeUid;
    private String payeeName;
    private String phoneNumber;
    private String payeeType;
    private String firstName;
    private String middleName;
    private String lastName;
    private String dateOfBirth;
    private String businessName;
    private List<PayeeAccountEntity> accounts;

    public String getPayeeUid() {
        return payeeUid;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPayeeType() {
        return payeeType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getBusinessName() {
        return businessName;
    }

    public boolean hasAccount(SortCodeIdentifier identifier) {

        if (accounts == null) {
            return false;
        }

        return accounts.stream().anyMatch(account -> account.equalsSortCodeIdentifer(identifier));
    }

    public Optional<PayeeAccountEntity> getAccount(SortCodeIdentifier identifier) {

        if (accounts == null) {
            return Optional.empty();
        }

        return accounts.stream()
                .filter(account -> account.equalsSortCodeIdentifer(identifier))
                .findAny();
    }

    public Stream<PayeeAccountEntity> streamAccounts() {
        return accounts.stream();
    }
}
