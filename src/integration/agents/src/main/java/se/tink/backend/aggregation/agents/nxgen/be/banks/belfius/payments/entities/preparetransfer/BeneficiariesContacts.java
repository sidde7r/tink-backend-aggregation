package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

@JsonObject
public class BeneficiariesContacts implements GeneralAccountEntity {
    @JsonProperty("mlb_ZipCode")
    private Text mlbZipCode;

    @JsonProperty("mlb_Alias")
    private Text mlbAlias;

    @JsonProperty("mlb_Key2_SequenceNumber")
    private Text mlbKey2SequenceNumber;

    @JsonProperty("mlb_Street")
    private Text mlbStreet;

    @JsonProperty("mlb_HouseNumber")
    private Text mlbHouseNumber;

    @JsonProperty("mlb_Ext_Int_Account")
    private Text mlbExtIntAccount;

    @JsonProperty("mlb_Communication")
    private Text mlbCommunication;

    @JsonProperty("mlb_Name")
    private Text mlbName;

    @JsonProperty("mlb_Amount")
    private Text mlbAmount;

    @JsonProperty("mlb_Key1_ListNumber")
    private Text mlbKey1ListNumber;

    @JsonProperty("mlb_StructuredCommunication")
    private Text mlbStructuredCommunication;

    @JsonProperty("mlb_City")
    private Text mlbCity;

    @JsonProperty("mlb_CodeCountry")
    private Text mlbCodeCountry;

    @JsonProperty("mlb_Contact")
    private Text mlbContact;

    @JsonProperty("mlb_Account")
    private Text mlbAccount;

    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Text getMlbZipCode() {
        return mlbZipCode;
    }

    public void setMlbZipCode(Text mlbZipCode) {
        this.mlbZipCode = mlbZipCode;
    }

    public Text getMlbAlias() {
        return mlbAlias;
    }

    public void setMlbAlias(Text mlbAlias) {
        this.mlbAlias = mlbAlias;
    }

    public Text getMlbKey2SequenceNumber() {
        return mlbKey2SequenceNumber;
    }

    public void setMlbKey2SequenceNumber(Text mlbKey2SequenceNumber) {
        this.mlbKey2SequenceNumber = mlbKey2SequenceNumber;
    }

    public Text getMlbStreet() {
        return mlbStreet;
    }

    public void setMlbStreet(Text mlbStreet) {
        this.mlbStreet = mlbStreet;
    }

    public Text getMlbHouseNumber() {
        return mlbHouseNumber;
    }

    public void setMlbHouseNumber(Text mlbHouseNumber) {
        this.mlbHouseNumber = mlbHouseNumber;
    }

    public Text getMlbExtIntAccount() {
        return mlbExtIntAccount;
    }

    public void setMlbExtIntAccount(Text mlbExtIntAccount) {
        this.mlbExtIntAccount = mlbExtIntAccount;
    }

    public Text getMlbCommunication() {
        return mlbCommunication;
    }

    public void setMlbCommunication(Text mlbCommunication) {
        this.mlbCommunication = mlbCommunication;
    }

    public Text getMlbName() {
        return mlbName;
    }

    public void setMlbName(Text mlbName) {
        this.mlbName = mlbName;
    }

    public Text getMlbAmount() {
        return mlbAmount;
    }

    public void setMlbAmount(Text mlbAmount) {
        this.mlbAmount = mlbAmount;
    }

    public Text getMlbKey1ListNumber() {
        return mlbKey1ListNumber;
    }

    public void setMlbKey1ListNumber(Text mlbKey1ListNumber) {
        this.mlbKey1ListNumber = mlbKey1ListNumber;
    }

    public Text getMlbStructuredCommunication() {
        return mlbStructuredCommunication;
    }

    public void setMlbStructuredCommunication(Text mlbStructuredCommunication) {
        this.mlbStructuredCommunication = mlbStructuredCommunication;
    }

    public Text getMlbCity() {
        return mlbCity;
    }

    public void setMlbCity(Text mlbCity) {
        this.mlbCity = mlbCity;
    }

    public Text getMlbCodeCountry() {
        return mlbCodeCountry;
    }

    public void setMlbCodeCountry(Text mlbCodeCountry) {
        this.mlbCodeCountry = mlbCodeCountry;
    }

    public Text getMlbContact() {
        return mlbContact;
    }

    public void setMlbContact(Text mlbContact) {
        this.mlbContact = mlbContact;
    }

    public Text getMlbAccount() {
        return mlbAccount;
    }

    public void setMlbAccount(Text mlbAccount) {
        this.mlbAccount = mlbAccount;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getAccount() {
        return mlbExtIntAccount.getText();
    }

    public boolean isAccount(String accountNum) {
        return mlbAccount.getText().replace(" ", "").equals(accountNum);
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SepaEurIdentifier(mlbAccount.getText().replace(" ", ""));
    }

    @Override
    public String generalGetBank() {
        return "--";
    }

    @Override
    public String generalGetName() {
        return mlbName.getText();
    }
}
