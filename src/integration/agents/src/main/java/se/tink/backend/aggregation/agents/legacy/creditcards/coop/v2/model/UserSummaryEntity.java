package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSummaryEntity {
    @JsonProperty("Accounts")
    private List<AccountEntity> accounts;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("HouseholdMembers")
    private List<String> houseHoldMembers;

    @JsonProperty("MemberAccountType")
    private int memberAccountType;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getHouseHoldMembers() {
        return houseHoldMembers;
    }

    public void setHouseHoldMembers(List<String> houseHoldMembers) {
        this.houseHoldMembers = houseHoldMembers;
    }

    public int getMemberAccountType() {
        return memberAccountType;
    }

    public void setMemberAccountType(int memberAccountType) {
        this.memberAccountType = memberAccountType;
    }
}
