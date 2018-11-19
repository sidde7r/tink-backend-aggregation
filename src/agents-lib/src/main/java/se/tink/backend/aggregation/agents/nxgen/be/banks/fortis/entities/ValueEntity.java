package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {
    @JsonProperty("distributorAuthenticationMeans")
    private List<DistributorAuthenticationMeansEntity> distributorAuthenticationMeans;
    @JsonProperty("authenticationMeanEligibilities")
    private List<AuthenticationMeanEligibilitiesEntity> authenticationMeanEligibilities;
    @JsonProperty("eBankingUsers")
    private List<EBankingUsersEntity> eBankingUsers;
    @JsonProperty("eBankingUserEligibilities")
    private List<EBankingUserEligibilitiesEntity> eBankingUserEligibilities;
    @JsonProperty("authenticationProcessId")
    private String authenticationProcessId;
    @JsonProperty("challenge")
    private ChallengeEntity challenge;
    @JsonProperty("challenges")
    private List<String> challenges;
    @JsonProperty("customerData")
    private CustomerDataEntity customerData;
    @JsonProperty("userData")
    private UserDataEntity userData;
    @JsonProperty("branchData")
    private BranchDataEntity branchData;
    @JsonProperty("illegalPasswords")
    private List<IllegalPasswordsEntity> illegalPasswords;
    @JsonProperty("minor")
    private String minor;
    @JsonProperty("parameters")
    private List<ParametersEntity> parameters;
    @JsonProperty("viewList")
    private List<ViewListEntity> viewList;
    @JsonProperty("newDocumentsToPayCounter")
    private int newDocumentsToPayCounter;
    @JsonProperty("newDocumentsToReadCounter")
    private int newDocumentsToReadCounter;

    public List<DistributorAuthenticationMeansEntity> getDistributorAuthenticationMeans() {
        return distributorAuthenticationMeans;
    }

    public List<AuthenticationMeanEligibilitiesEntity> getAuthenticationMeanEligibilities() {
        return authenticationMeanEligibilities;
    }

    public List<EBankingUsersEntity> geteBankingUsers() {
        return eBankingUsers;
    }

    public List<EBankingUserEligibilitiesEntity> geteBankingUserEligibilities() {
        return eBankingUserEligibilities;
    }

    public String getAuthenticationProcessId() {
        return authenticationProcessId;
    }

    public ChallengeEntity getChallenge() {
        return challenge;
    }

    public List<String> getChallenges() {
        return challenges;
    }

    public CustomerDataEntity getCustomerData() {
        return customerData;
    }

    public UserDataEntity getUserData() {
        return userData;
    }

    public BranchDataEntity getBranchData() {
        return branchData;
    }

    public List<IllegalPasswordsEntity> getIllegalPasswords() {
        return illegalPasswords;
    }

    public String getMinor() {
        return minor;
    }

    public List<ParametersEntity> getParameters() {
        return parameters;
    }

    public List<ViewListEntity> getViewList() {
        return viewList;
    }

    public int getNewDocumentsToPayCounter() {
        return newDocumentsToPayCounter;
    }

    public int getNewDocumentsToReadCounter() {
        return newDocumentsToReadCounter;
    }
}
