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
    @JsonProperty("challenges")
    private List<ChallengesEntity> challenges;
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
    @JsonProperty("parameters")
    private int newDocumentsToPayCounter;
    @JsonProperty("parameters")
    private int newDocumentsToReadCounter;

}
