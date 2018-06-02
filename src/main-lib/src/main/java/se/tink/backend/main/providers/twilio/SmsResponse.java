package se.tink.backend.main.providers.twilio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SmsResponse {
    private String sid;
    @JsonProperty("date_created")
    private Date dateCreated;
    @JsonProperty("date_updated")
    private Date dateUpdated;
    @JsonProperty("date_sent")
    private Date dateSent;
    @JsonProperty("account_sid")
    private String accountSid;
    private String to;
    private String from;
    @JsonProperty("messaging_service_sid")
    private String messagingServiceSid;
    private String body;
    private String status;
    @JsonProperty("num_segments")
    private String numSegments;
    @JsonProperty("num_media")
    private String numMedia;
    private String direction;
    @JsonProperty("api_version")
    private String apiVersion;
    private String price;
    @JsonProperty("price_unit")
    private String priceUnit;
    @JsonProperty("error_code")
    private String errorCode;
    @JsonProperty("error_message")
    private String errorMessage;
    private String uri;
    @JsonProperty("subresource_uris")
    private HashMap<String, String> subresourceUris;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    public String getAccountSid() {
        return accountSid;
    }

    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessagingServiceSid() {
        return messagingServiceSid;
    }

    public void setMessagingServiceSid(String messagingServiceSid) {
        this.messagingServiceSid = messagingServiceSid;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNumSegments() {
        return numSegments;
    }

    public void setNumSegments(String numSegments) {
        this.numSegments = numSegments;
    }

    public String getNumMedia() {
        return numMedia;
    }

    public void setNumMedia(String numMedia) {
        this.numMedia = numMedia;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HashMap<String, String> getSubresourceUris() {
        return subresourceUris;
    }

    public void setSubresourceUris(HashMap<String, String> subresourceUris) {
        this.subresourceUris = subresourceUris;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sid", sid)
                .add("date_created", dateCreated)
                .add("date_pdated", dateUpdated)
                .add("date_sent", dateSent)
                .add("account_sid", accountSid)
                .add("to", to)
                .add("from", from)
                .add("messaging_service_sid", messagingServiceSid)
                .add("body", body)
                .add("status", status)
                .add("num_segments", numSegments)
                .add("num_media", numMedia)
                .add("direction", direction)
                .add("api_version", apiVersion)
                .add("price", price)
                .add("price_unit", priceUnit)
                .add("error_code", errorCode)
                .add("error_message", errorMessage)
                .add("uri", uri)
                .add("subresource_uris", subresourceUris)
                .toString();
    }
}
