package se.tink.backend.aggregation.agents.banks.seb.model;

public interface MatchableTransferRequestEntity {
    boolean matches(TransferListEntity transferListEntity);
}
