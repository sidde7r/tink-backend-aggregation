package se.tink.backend.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Type;
import se.tink.backend.utils.StringUtils;

@Entity
@Table(name = "abnamro_buffered_transactions")
@IdClass(AbnAmroBufferedTransactionsPk.class)
public class AbnAmroBufferedTransaction {

    @Id
    @Column(name="`id`")
    private String id;
    @Id
    @Column(name="`credentialsid`")
    private String credentialsId;

    private long accountNumber;
    // The fields below map to se.tink.bankend.connector.abnamro.model.TransactionEntity
    private double amount;
    private String cpAccount;
    private String cpName;
    @Column(columnDefinition = "DATETIME(6)")
    private Date date;
    @Type(type = "text")
    private String description;
    private String originType;
    @Type(type = "text")
    private String payload;
    private String type;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final TypeReference<List<String>> DESCRIPTION_TYPE_REFERENCE = new TypeReference<List<String>>() {

    };
    
    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {

    };
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public double getAmount() {
        return amount;
    }

    public String getCpAccount() {
        return cpAccount;
    }
    
    public String getCpName() {
        return cpName;
    }
    
    public String getCredentialsId() {
        return credentialsId;
    }
    
    public Date getDate() {
        return date;
    }
    
    public List<String> getDescription() {
        try {
            if (Strings.isNullOrEmpty(description)) {
                return Lists.newArrayList();
            } else {
                return OBJECT_MAPPER.readValue(description, DESCRIPTION_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getId() {
        generateIdIfMissing();
        return id;
    }

    public String getOriginType() {
        return originType;
    }

    public Map<String, Object> getPayload() {
        try {
            if (Strings.isNullOrEmpty(payload)) {
                return Maps.newHashMap();
            } else {
                return OBJECT_MAPPER.readValue(payload, PAYLOAD_TYPE_REFERENCE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getType() {
        return type;
    }
    
    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setCpAccount(String cpAccount) {
        this.cpAccount = cpAccount;
    }

    public void setCpName(String cpName) {
        this.cpName = cpName;
    }
    
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    public void setDescription(List<String> description) {
        try {
            this.description = OBJECT_MAPPER.writeValueAsString(description);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void setOriginType(String originType) {
        this.originType = originType;
    }

    public void setPayload(Map<String, Object> payload) {
        try {
            this.payload = OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    @PrePersist
    private void generateIdIfMissing() {
        if (id == null) {
            id = StringUtils.generateUUID();
        }
    }
}
