package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcServiceConfigurationEntity {
    private boolean saxoTrader;
    private boolean loan;
    private boolean giro;
    private boolean transfer;
    private boolean outbox;
    private boolean epayment;
    private boolean creditCard;
    private boolean cardsOverview;
    private boolean blockCard;
    private boolean custody;
    private boolean accounts;
    private boolean sharedAgreementAccounts;
    private boolean spendingOverview;
    private boolean communicationRead;
    private boolean communicationWrite;
    private boolean netmeeting;
    private SdcInvestmentEntity investment;
    private boolean bsAgreementsShow;
    private boolean bsAgreementsDecline;
    private boolean bsAgreementsCreate;
    private boolean regularTransfersShow;
    private boolean regularTransfersCreate;
    private boolean regularTransfersDecline;
    private boolean aftaleGiroShow;
    private boolean aftaleGiroCreate;
    private boolean aftaleGiroDelete;
    private boolean autoGiroReceiver;
    private boolean autoGiroList;
    private boolean autoGiroRegisterSign;
    private boolean autoGiroCancel;
    private boolean eCardAgreementsShow;
    private boolean eCardCreate;
    private boolean eCardChange;
    private boolean eCardToBS;
    private boolean bsToECard;
    private boolean eInvoiceAgreementShow;
    private boolean eInvoiceCreate;
    private boolean eInvoiceChange;
    private boolean fbfPrimaryOwner;
    private boolean snapCash;
    private boolean userNotificationsServicesRead;
    private boolean userNotificationsServicesWrite;
    private boolean totalkredit;

    public boolean isSaxoTrader() {
        return saxoTrader;
    }

    public boolean isLoan() {
        return loan;
    }

    public boolean isGiro() {
        return giro;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public boolean isOutbox() {
        return outbox;
    }

    public boolean isEpayment() {
        return epayment;
    }

    public boolean isCreditCard() {
        return creditCard;
    }

    public boolean isCardsOverview() {
        return cardsOverview;
    }

    public boolean isBlockCard() {
        return blockCard;
    }

    public boolean isCustody() {
        return custody;
    }

    public boolean isAccounts() {
        return accounts;
    }

    public boolean isSharedAgreementAccounts() {
        return sharedAgreementAccounts;
    }

    public boolean isSpendingOverview() {
        return spendingOverview;
    }

    public boolean isCommunicationRead() {
        return communicationRead;
    }

    public boolean isCommunicationWrite() {
        return communicationWrite;
    }

    public boolean isNetmeeting() {
        return netmeeting;
    }

    public SdcInvestmentEntity getInvestment() {
        return investment;
    }

    public boolean isBsAgreementsShow() {
        return bsAgreementsShow;
    }

    public boolean isBsAgreementsDecline() {
        return bsAgreementsDecline;
    }

    public boolean isBsAgreementsCreate() {
        return bsAgreementsCreate;
    }

    public boolean isRegularTransfersShow() {
        return regularTransfersShow;
    }

    public boolean isRegularTransfersCreate() {
        return regularTransfersCreate;
    }

    public boolean isRegularTransfersDecline() {
        return regularTransfersDecline;
    }

    public boolean isAftaleGiroShow() {
        return aftaleGiroShow;
    }

    public boolean isAftaleGiroCreate() {
        return aftaleGiroCreate;
    }

    public boolean isAftaleGiroDelete() {
        return aftaleGiroDelete;
    }

    public boolean isAutoGiroReceiver() {
        return autoGiroReceiver;
    }

    public boolean isAutoGiroList() {
        return autoGiroList;
    }

    public boolean isAutoGiroRegisterSign() {
        return autoGiroRegisterSign;
    }

    public boolean isAutoGiroCancel() {
        return autoGiroCancel;
    }

    public boolean iseCardAgreementsShow() {
        return eCardAgreementsShow;
    }

    public boolean iseCardCreate() {
        return eCardCreate;
    }

    public boolean iseCardChange() {
        return eCardChange;
    }

    public boolean iseCardToBS() {
        return eCardToBS;
    }

    public boolean isBsToECard() {
        return bsToECard;
    }

    public boolean iseInvoiceAgreementShow() {
        return eInvoiceAgreementShow;
    }

    public boolean iseInvoiceCreate() {
        return eInvoiceCreate;
    }

    public boolean iseInvoiceChange() {
        return eInvoiceChange;
    }

    public boolean isFbfPrimaryOwner() {
        return fbfPrimaryOwner;
    }

    public boolean isSnapCash() {
        return snapCash;
    }

    public boolean isUserNotificationsServicesRead() {
        return userNotificationsServicesRead;
    }

    public boolean isUserNotificationsServicesWrite() {
        return userNotificationsServicesWrite;
    }

    public boolean isTotalkredit() {
        return totalkredit;
    }

    public boolean isInvestmentDeposit() {
        return investment.isDeposit();
    }
}
