package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Notifications{

	@JsonProperty("card_validity")
	private boolean cardValidity;

	@JsonProperty("einvoice_in")
	private boolean einvoiceIn;

	@JsonProperty("payment_pending")
	private boolean paymentPending;

	@JsonProperty("payment_in")
	private boolean paymentIn;

	@JsonProperty("payment_fail")
	private boolean paymentFail;

	@JsonProperty("account_limit_reached")
	private boolean accountLimitReached;

	public void setCardValidity(boolean cardValidity){
		this.cardValidity = cardValidity;
	}

	public boolean isCardValidity(){
		return cardValidity;
	}

	public void setEinvoiceIn(boolean einvoiceIn){
		this.einvoiceIn = einvoiceIn;
	}

	public boolean isEinvoiceIn(){
		return einvoiceIn;
	}

	public void setPaymentPending(boolean paymentPending){
		this.paymentPending = paymentPending;
	}

	public boolean isPaymentPending(){
		return paymentPending;
	}

	public void setPaymentIn(boolean paymentIn){
		this.paymentIn = paymentIn;
	}

	public boolean isPaymentIn(){
		return paymentIn;
	}

	public void setPaymentFail(boolean paymentFail){
		this.paymentFail = paymentFail;
	}

	public boolean isPaymentFail(){
		return paymentFail;
	}

	public void setAccountLimitReached(boolean accountLimitReached){
		this.accountLimitReached = accountLimitReached;
	}

	public boolean isAccountLimitReached(){
		return accountLimitReached;
	}

	@Override
 	public String toString(){
		return 
			"Notifications{" + 
			"card_validity = '" + cardValidity + '\'' + 
			",einvoice_in = '" + einvoiceIn + '\'' + 
			",payment_pending = '" + paymentPending + '\'' + 
			",payment_in = '" + paymentIn + '\'' + 
			",payment_fail = '" + paymentFail + '\'' + 
			",account_limit_reached = '" + accountLimitReached + '\'' + 
			"}";
		}
}