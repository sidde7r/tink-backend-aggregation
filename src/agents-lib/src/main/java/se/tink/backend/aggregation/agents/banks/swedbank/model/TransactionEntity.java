package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionEntity {
	protected String amount;
	protected String currency;
	protected String date;
	protected String description;
	protected AmountEntity localAmount;

	public String getAmount() {
		return amount;
	}

    public String getCurrency() {
		return currency;
	}

    public String getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public AmountEntity getLocalAmount() {
        return localAmount;
    }

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setLocalAmount(AmountEntity localAmount) {
        this.localAmount = localAmount;
    }

}
