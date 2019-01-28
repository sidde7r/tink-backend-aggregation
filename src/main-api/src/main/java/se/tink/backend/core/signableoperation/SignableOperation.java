package se.tink.backend.core.signableoperation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.UUID;
import se.tink.backend.core.User;
import se.tink.libraries.enums.SignableOperationTypes;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SignableOperation {
    private static final ImmutableSet<SignableOperationStatuses> IN_PROGRESS_STATUSES = ImmutableSet.of(
            SignableOperationStatuses.EXECUTING, SignableOperationStatuses.AWAITING_CREDENTIALS,
            SignableOperationStatuses.CREATED
    );

    @Tag(1)
    @ApiModelProperty(name = "created", example = "1471349422000", value = "The timestamp of the creation of the operation.")
    private Date created;
    @Tag(2)
    @ApiModelProperty(name = "id", example = "a4516bda6ff545e0aa24e54b859579e0", value = "The id of this operation.")
    private UUID id;
    @Tag(3)
    @ApiModelProperty(name = "status", example = "EXECUTED", value = "The status of the operation. CANCELLED, FAILED and EXECUTED are all endstates.", allowableValues = SignableOperationStatuses.DOCUMENTED)
    private String status;
    @Exclude
    @ApiModelProperty(name = "statusDetailsKey", hidden = true)
    private StatusDetailsKey statusDetailsKey;
    @Tag(4)
    @ApiModelProperty(name = "statusMessage", example = "The transfer has been sent to your bank.", value = "The status message of the operation")
    private String statusMessage;
    @Tag(5)
    @ApiModelProperty(name = "type", example = "TRANSFER", value = "The type of operation", allowableValues = SignableOperationTypes.DOCUMENTED)
    private String type;
    @Tag(6)
    @ApiModelProperty(name = "underlyingId", example = "1e09bab571d84b1cbe8d49c0be9c030f", value = "The id of the actual underlying operation (e.g. the id of a Transfer operation).")
    private UUID underlyingId;
    @Tag(7)
    @ApiModelProperty(name = "updated", example = "1471349422000", value = "The timestamp of the last update of the operation.")
    private Date updated;
    @Tag(8)
    @ApiModelProperty(name = "userId", example = "2f37e3ff1e5342b39c41bee3ee73cf8e", value = "The id of the user making the operation.")
    private UUID userId;
    @Tag(9)
    @ApiModelProperty(name = "credentialsId", example = "342220f1e0484c0481b2b468d7fbcfc4", value = "The id of the Credentials used to make the operation.")
    private UUID credentialsId;
    @Exclude
    @ApiModelProperty(name = "signableObject", hidden = true)
    private String signableObject;

    public SignableOperation() {

    }

    public SignableOperation(Transfer transfer) {
        this.underlyingId = transfer.getId();
        this.userId = transfer.getUserId();
        this.credentialsId = transfer.getCredentialsId();
        this.type = SignableOperationTypes.TRANSFER.name();
        this.signableObject = SerializationUtils.serializeToString(transfer);
    }

    public SignableOperation(GenericApplication application) {
        this.underlyingId = application.getApplicationId();
        this.userId = application.getUserId();
        this.credentialsId = application.getCredentialsId();
        this.type = SignableOperationTypes.APPLICATION.name();
    }

    public Date getCreated() {
        return created;
    }

    public UUID getId() {
        return id;
    }

    public SignableOperationStatuses getStatus() {
        if (status == null) {
            return null;
        }
        return SignableOperationStatuses.valueOf(status);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public StatusDetailsKey getStatusDetailsKey() {
        return statusDetailsKey;
    }

    public String getStatusMessage() {
        return Strings.isNullOrEmpty(statusMessage) ? getDefaultStatusMessage() : statusMessage;
    }

    private String getDefaultStatusMessage() {
        if (statusDetailsKey == null) {
            return null;
        }

        switch (statusDetailsKey) {
        case TECHNICAL_ERROR:
            return "Ett tekniskt fel uppstod, vänligen försök igen senare";
        case INVALID_INPUT:
            return "Ett eller flera felaktigt ifyllda fält, vänligen kontakta Tinks support: support@tink.se";
        case BANKID_FAILED:
            return "BankID signeringen misslyckades, vänligen försök igen";
        default:
            return null;
        }
    }

    public SignableOperationTypes getType() {
        if (type == null) {
            return null;
        }
        return SignableOperationTypes.valueOf(type);
    }

    public UUID getUnderlyingId() {
        return underlyingId;
    }

    public Date getUpdated() {
        return updated;
    }

    public UUID getUserId() {
        return userId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSignableObject() {
        return signableObject;
    }

    public <T> T getSignableObject(Class<T> cls) {
        return SerializationUtils.deserializeFromString(signableObject, cls);
    }

    @JsonIgnore
    public boolean isInProgress() {
        return IN_PROGRESS_STATUSES.contains(getStatus());
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setStatus(SignableOperationStatuses status) {
        if (status == null) {
            this.status = null;
        } else {
            this.status = status.name();
        }
    }

    public void setStatusDetailsKey(StatusDetailsKey statusDetailsKey) {
        this.statusDetailsKey = statusDetailsKey;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setType(SignableOperationTypes type) {
        this.type = type.name();
    }

    public void setUnderlyingId(UUID underlyingId) {
        this.underlyingId = underlyingId;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(UUID credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setSignableObject(String signableObject) {
        this.signableObject = signableObject;
    }

    public static SignableOperation create(Transfer transfer, SignableOperationStatuses status) {
        SignableOperation signableOperation = transfer != null ?
                new SignableOperation(transfer) : new SignableOperation();

        Date now = new Date();
        signableOperation.setStatus(status);
        signableOperation.setCreated(now);
        signableOperation.setUpdated(now);

        return signableOperation;
    }

    public static SignableOperation create(User user, SignableOperationStatuses status) {
        SignableOperation signableOperation = new SignableOperation();

        if (user != null && !Strings.isNullOrEmpty(user.getId())) {
            signableOperation.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        }

        Date now = new Date();
        signableOperation.setStatus(status);
        signableOperation.setCreated(now);
        signableOperation.setUpdated(now);

        return signableOperation;
    }

    public static SignableOperation create(GenericApplication application, SignableOperationStatuses status) {
        SignableOperation signableOperation = application != null ?
                new SignableOperation(application) : new SignableOperation();

        Date now = new Date();
        signableOperation.setStatus(status);
        signableOperation.setCreated(now);
        signableOperation.setUpdated(now);

        return signableOperation;
    }

    @JsonIgnore
    public void cleanSensitiveData() {
        signableObject = null;
        statusDetailsKey = null;
    }

    public enum StatusDetailsKey {
        BANKID_FAILED, INVALID_INPUT, TECHNICAL_ERROR, USER_VALIDATION_ERROR
    }
}
