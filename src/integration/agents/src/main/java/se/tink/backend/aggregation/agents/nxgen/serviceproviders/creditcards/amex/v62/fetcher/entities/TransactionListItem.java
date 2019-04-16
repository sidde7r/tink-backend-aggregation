package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionListItem{

	@JsonProperty("secondaryId")
	private String secondaryId;

	@JsonProperty("amount")
	private Amount amount;

	@JsonProperty("transactionReference")
	private String transactionReference;

	@JsonProperty("suppIndex")
	private String suppIndex;

	@JsonProperty("description")
	private List<String> description;

	@JsonProperty("billingCycleIndex")
	private int billingCycleIndex;

	@JsonProperty("type")
	private String type;

	@JsonProperty("transactionId")
	private String transactionId;

	@JsonProperty("billingCycleDate")
	private int billingCycleDate;

	@JsonProperty("displaySuppIcon")
	private boolean displaySuppIcon;

	@JsonProperty("extendedTransactionDetails")
	private ExtendedTransactionDetails extendedTransactionDetails;

	@JsonProperty("chargeDate")
	private ChargeDate chargeDate;

	@JsonProperty("formattedAmount")
	private String formattedAmount;

	@JsonProperty("transTypeDesc")
	private String transTypeDesc;

	@JsonProperty("cardMemberName")
	private String cardMemberName;

	public String getSecondaryId(){
		return secondaryId;
	}

	public Amount getAmount(){
		return amount;
	}

	public String getTransactionReference(){
		return transactionReference;
	}

	public String getSuppIndex(){
		return suppIndex;
	}

	public List<String> getDescription(){
		return description;
	}

	public int getBillingCycleIndex(){
		return billingCycleIndex;
	}

	public String getType(){
		return type;
	}

	public String getTransactionId(){
		return transactionId;
	}

	public int getBillingCycleDate(){
		return billingCycleDate;
	}

	public boolean isDisplaySuppIcon(){
		return displaySuppIcon;
	}

	public ExtendedTransactionDetails getExtendedTransactionDetails(){
		return extendedTransactionDetails;
	}

	public ChargeDate getChargeDate(){
		return chargeDate;
	}

	public String getFormattedAmount(){
		return formattedAmount;
	}

	public String getTransTypeDesc(){
		return transTypeDesc;
	}

	public String getCardMemberName(){
		return cardMemberName;
	}
}