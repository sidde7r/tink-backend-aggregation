package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
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
        return Optional.ofNullable(signatures).orElseGet(Collections::emptyList).stream()
                .map(SignatureEntity::getId)
                .findFirst()
                .orElseThrow(
                        () ->
                                TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                                        .setMessage(catalog.getString(EndUserMessage.BANKID_FAILED))
                                        .setEndUserMessage(
                                                catalog.getString(EndUserMessage.BANKID_FAILED))
                                        .build());
    }
}
