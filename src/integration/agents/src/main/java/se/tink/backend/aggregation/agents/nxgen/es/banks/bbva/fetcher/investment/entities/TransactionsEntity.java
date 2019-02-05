package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private boolean allowsConsult;
    private boolean allowsOperate;
    private boolean allowsDetailConsult;
    private boolean allowsConsultChange;
    private boolean allowsOperateChange;
    private boolean canDoStockAccountInternalTransfer;
    private boolean canReceiveStockAccountInternalTransfer;
    private boolean allowsStockBuying;
    private boolean allowsStockSelling;
}
