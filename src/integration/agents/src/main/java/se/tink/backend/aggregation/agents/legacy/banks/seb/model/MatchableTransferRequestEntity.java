package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

public interface MatchableTransferRequestEntity {
    boolean matches(TransferListEntity transferListEntity);
}
