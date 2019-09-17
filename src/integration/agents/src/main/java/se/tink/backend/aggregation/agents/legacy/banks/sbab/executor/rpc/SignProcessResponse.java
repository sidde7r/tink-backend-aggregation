package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.SignatureEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
public class SignProcessResponse {
    private String id;
    private String status;
    private List<SignatureEntity> signatures;

    @JsonIgnore
    public String getBankIdRefOrThrowIfNotPresent(Catalog catalog) {
        if (signatureIsPresent()) {
            SignatureEntity signature = signatures.get(0);
            String bankIdRef = signature.getId();

            if (!Strings.isNullOrEmpty(bankIdRef)) {
                return bankIdRef;
            }
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(catalog.getString(EndUserMessage.BANKID_FAILED))
                .setEndUserMessage(catalog.getString(EndUserMessage.BANKID_FAILED))
                .build();
    }

    @JsonIgnore
    private boolean signatureIsPresent() {
        return signatures != null && !signatures.isEmpty();
    }
}
