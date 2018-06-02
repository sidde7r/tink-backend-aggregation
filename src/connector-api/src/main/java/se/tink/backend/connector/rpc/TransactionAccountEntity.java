package se.tink.backend.connector.rpc;

import java.util.Map;

public interface TransactionAccountEntity {

    Double getBalance();

    Double getReservedAmount();

    String getExternalId();

    Map<String, Object> getPayload();

    void setBalance(Double balance);

    void setReservedAmount(Double reservedAmount);

    void setExternalId(String externalId);

    void setPayload(Map<String, Object> payload);
}
