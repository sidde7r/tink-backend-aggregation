package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Card {

	@JsonProperty("owner_name")
	private String ownerName;

	@JsonProperty("additional")
	private boolean additional;

	@JsonProperty("credit_interest_amount")
	private double creditInterestAmount;

	@JsonProperty("credit_reserved_amount")
	private double creditReservedAmount;

	@JsonProperty("product_code")
	private String productCode;

	@JsonProperty("type")
	private String type;

	@JsonProperty("credit_free_amount")
	private double creditFreeAmount;

	@JsonProperty("limit_portfolio_id")
	private int limitPortfolioId;

	@JsonProperty("product_name")
	private String productName;

	@JsonProperty("card_id")
	private int cardId;

	@JsonProperty("secondary_number")
	private String secondaryNumber;

	@JsonProperty("number")
	private String number;

	@JsonProperty("valid_until")
	private String validUntil;

	@JsonProperty("business_name_on_card")
	private Object businessNameOnCard;

	@JsonProperty("product_class_code")
	private String productClassCode;

	@JsonProperty("service_portfolio_id")
	private int servicePortfolioId;

	@JsonProperty("credit_limit_amount")
	private double creditLimitAmount;

	@JsonProperty("color_code")
	private String colorCode;

	@JsonProperty("name_on_card")
	private String nameOnCard;

	@JsonProperty("status")
	private String status;

	public void setOwnerName(String ownerName){
		this.ownerName = ownerName;
	}

	public String getOwnerName(){
		return ownerName;
	}

	public void setAdditional(boolean additional){
		this.additional = additional;
	}

	public boolean isAdditional(){
		return additional;
	}

	public void setCreditInterestAmount(double creditInterestAmount){
		this.creditInterestAmount = creditInterestAmount;
	}

	public double getCreditInterestAmount(){
		return creditInterestAmount;
	}

	public void setCreditReservedAmount(double creditReservedAmount){
		this.creditReservedAmount = creditReservedAmount;
	}

	public double getCreditReservedAmount(){
		return creditReservedAmount;
	}

	public void setProductCode(String productCode){
		this.productCode = productCode;
	}

	public String getProductCode(){
		return productCode;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setCreditFreeAmount(double creditFreeAmount){
		this.creditFreeAmount = creditFreeAmount;
	}

	public double getCreditFreeAmount(){
		return creditFreeAmount;
	}

	public void setLimitPortfolioId(int limitPortfolioId){
		this.limitPortfolioId = limitPortfolioId;
	}

	public int getLimitPortfolioId(){
		return limitPortfolioId;
	}

	public void setProductName(String productName){
		this.productName = productName;
	}

	public String getProductName(){
		return productName;
	}

	public void setCardId(int cardId){
		this.cardId = cardId;
	}

	public int getCardId(){
		return cardId;
	}

	public void setSecondaryNumber(String secondaryNumber){
		this.secondaryNumber = secondaryNumber;
	}

	public String getSecondaryNumber(){
		return secondaryNumber;
	}

	public void setNumber(String number){
		this.number = number;
	}

	public String getNumber(){
		return number;
	}

	public void setValidUntil(String validUntil){
		this.validUntil = validUntil;
	}

	public String getValidUntil(){
		return validUntil;
	}

	public void setBusinessNameOnCard(Object businessNameOnCard){
		this.businessNameOnCard = businessNameOnCard;
	}

	public Object getBusinessNameOnCard(){
		return businessNameOnCard;
	}

	public void setProductClassCode(String productClassCode){
		this.productClassCode = productClassCode;
	}

	public String getProductClassCode(){
		return productClassCode;
	}

	public void setServicePortfolioId(int servicePortfolioId){
		this.servicePortfolioId = servicePortfolioId;
	}

	public int getServicePortfolioId(){
		return servicePortfolioId;
	}

	public void setCreditLimitAmount(double creditLimitAmount){
		this.creditLimitAmount = creditLimitAmount;
	}

	public double getCreditLimitAmount(){
		return creditLimitAmount;
	}

	public void setColorCode(String colorCode){
		this.colorCode = colorCode;
	}

	public String getColorCode(){
		return colorCode;
	}

	public void setNameOnCard(String nameOnCard){
		this.nameOnCard = nameOnCard;
	}

	public String getNameOnCard(){
		return nameOnCard;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	@Override
 	public String toString(){
		return 
			"CardsItem{" + 
			"owner_name = '" + ownerName + '\'' + 
			",additional = '" + additional + '\'' + 
			",credit_interest_amount = '" + creditInterestAmount + '\'' + 
			",credit_reserved_amount = '" + creditReservedAmount + '\'' + 
			",product_code = '" + productCode + '\'' + 
			",type = '" + type + '\'' + 
			",credit_free_amount = '" + creditFreeAmount + '\'' + 
			",limit_portfolio_id = '" + limitPortfolioId + '\'' + 
			",product_name = '" + productName + '\'' + 
			",card_id = '" + cardId + '\'' + 
			",secondary_number = '" + secondaryNumber + '\'' + 
			",number = '" + number + '\'' + 
			",valid_until = '" + validUntil + '\'' + 
			",business_name_on_card = '" + businessNameOnCard + '\'' + 
			",product_class_code = '" + productClassCode + '\'' + 
			",service_portfolio_id = '" + servicePortfolioId + '\'' + 
			",credit_limit_amount = '" + creditLimitAmount + '\'' + 
			",color_code = '" + colorCode + '\'' + 
			",name_on_card = '" + nameOnCard + '\'' + 
			",status = '" + status + '\'' + 
			"}";
		}
}