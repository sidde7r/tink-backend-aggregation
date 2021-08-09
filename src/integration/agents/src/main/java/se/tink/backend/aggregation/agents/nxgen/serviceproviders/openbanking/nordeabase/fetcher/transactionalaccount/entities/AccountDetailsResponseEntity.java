package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetailsResponseEntity {

    private String id;
    private String country;

    @JsonProperty("account_numbers")
    private List<AccountNumberEntity> accountNumbers;

    private String currency;

    @JsonProperty("account_name")
    private String accountName;

    private String product;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("available_balance")
    private double availableBalance;

    @JsonProperty("booked_balance")
    private double bookedBalance;

    @JsonProperty("value_dated_balance")
    private double valueDatedBalance;

    private BankEntity bank;
    private String status;

    @JsonProperty("credit_limit")
    private String creditLimit;

    @JsonProperty("latest_transaction_booking_date")
    private String latestTransactionBookingDate;

    @JsonProperty("_links")
    private List<LinkEntity> links;
}
