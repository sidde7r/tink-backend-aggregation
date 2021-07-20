package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrivilegeListEntity {

    @JsonFormat(
            with = {
                JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
            })
    List<String> accountNumber;

    @JsonFormat(
            with = {
                JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
                JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY
            })
    List<String> accountId;

    /**
     * If there is accountId in the response - it has higher priority than accountNumber. This
     * identifier is used for fetching account details.
     *
     * @return
     */
    @JsonIgnore
    public List<String> getAccountIdentifier() {
        if (CollectionUtils.isNotEmpty(accountId)) {
            return accountId;
        }
        return accountNumber;
    }

    // Post API
    @JsonProperty("ais-accounts:getAccounts")
    PrivilegeItemEntity aisAccountsGetAccounts;

    @JsonProperty("ais:getTransactionsRejected")
    PrivilegeItemWithHistoryEntity aisGetTransactionsRejected;

    @JsonProperty("ais:getTransactionsDone")
    PrivilegeItemWithHistoryEntity aisGetTransactionsDone;

    @JsonProperty("ais:getTransactionsCancelled")
    PrivilegeItemWithHistoryEntity aisGetTransactionsCancelled;

    @JsonProperty("ais:getAccount")
    PrivilegeItemEntity aisGetAccount;

    @JsonProperty("ais:getHolds")
    PrivilegeItemWithHistoryEntity aisGetHolds;

    @JsonProperty("ais:getTransactionsPending")
    PrivilegeItemWithHistoryEntity aisGetTransactionsPending;

    @JsonProperty("ais:getTransactionsScheduled")
    PrivilegeItemWithHistoryEntity aisGetTransactionsScheduled;

    // Get API
    @JsonProperty("ais-accounts:accounts")
    PrivilegeItemEntity aisAccountsAccounts;

    @JsonProperty("ais:transactions")
    PrivilegeItemWithHistoryAndTransactionStatusEntity aisTransactions;

    @JsonProperty("ais:accountDetails")
    PrivilegeItemEntity aisAccountDetails;
}
