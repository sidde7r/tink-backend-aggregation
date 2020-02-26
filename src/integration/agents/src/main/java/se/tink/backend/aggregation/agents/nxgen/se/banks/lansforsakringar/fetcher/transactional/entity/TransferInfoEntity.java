package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferInfoEntity {
  private String toText;
  private String fromText;
  private boolean transferToAccountInternal;
  private String toAccountBankName;
  private boolean recurring;
  private String recurringFrequencyText;
  private String finalDate;
  // `thirdPartyProviderName` is null - cannot define it!
  // `thirdPartyProviderPaymentReference` is null - cannot define it!
}