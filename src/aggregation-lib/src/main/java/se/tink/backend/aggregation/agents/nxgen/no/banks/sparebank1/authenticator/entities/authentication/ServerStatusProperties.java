package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerStatusProperties {
    private boolean SMS_PAYMENT;
    private boolean CREATE_ACCOUNT_UNDER_18;
    private boolean PAYMENT_RECEIPT;
    private boolean CONSENTS_TRIGGER;
    private boolean PENSION;
    private boolean CARDS;
    private boolean AKSJESPAREKONTO_OVERVIEW_TRIGGER;
    private boolean USE_BETALING_APP;
    private boolean APP_CREATE_TOKEN;
    private boolean CARD_SHOW_PIN;
    private boolean YOUTH_INSURANCE_MY_OVERVIEW;
    private boolean CREDIT_LIMIT_TRIGGER;
    private boolean APP_LOGIN_FINGERPRINT_IOS;
    private boolean AKSJESPAREKONTO_IN_PM_SPARING;
    private boolean TRAVEL_INSURANCE_MY_OVERVIEW;
    private boolean STRAKSBETALING;
    private boolean CARD_BLOCK_CARD;
    private boolean TRAVEL_INSURANCE;
    private boolean NEW_CREDIT_CARD_APPLICATION_RESPONSIVE;
    private boolean INSURANCE_EXTERNAL;
    private boolean CARD_SAVINGS;
    private boolean APP_LOGIN_SRP;
    private boolean CONSENTS_VIEW;
    private boolean CREDIT_LIMIT;
    private boolean AKSJESPAREKONTO;
    private boolean ABOUT_ME_SETTINGS;
    private boolean AKSJESPAREKONTO_ADVERTISEMENT;
    private boolean YOUTH_INSURANCE_TRIGGER;
    private boolean CONSENTS_EDIT;
    private boolean PENSION_TRIGGER;
    private boolean APP_LOGIN_WEAK_TOKEN;
    private boolean INSURANCE;
    private boolean FUND_TRADING;
    private boolean CHARGING;
    private boolean TRAVEL_INSURANCE_ORDER;
    private boolean APP_LOGIN_FINGERPRINT_ANDROID;
    private boolean PAYMENT_TO_CREDIT_CARD;
    private boolean SMS_NOTIFICATION;
    private boolean loginEnabled;
    private boolean CONSENTS;
    private boolean BECOME_CUSTOMER;
    private boolean DIALOG;
    private boolean FUND;
    private boolean ADOBE_CAMPAIGN;
    private boolean APP_DOWNLOAD_TRAVELINSURANCE;
    private boolean PAYMENT_INSURANCE;
    private boolean USE_IFRAME_TRAVEL_INSURANCE;
    private boolean CAR_INSURANCE_MY_OVERVIEW;
    private boolean PENSION_MY_OVERVIEW;
    private boolean SAVINGS;
    private boolean SAVINGS_TRIGGER;
    private boolean TRAVEL_INSURANCE_TRIGGER;
    private boolean REGIONAL_BLOCKING;
    private boolean CREATE_ACCOUNT;
    private boolean PAYMENT_RENDER_CID_BY_DEFAULT;
    private boolean SUBMIT_FRAUD_CLAIM;

    public boolean isSMS_PAYMENT() {
        return SMS_PAYMENT;
    }

    public void setSMS_PAYMENT(boolean SMS_PAYMENT) {
        this.SMS_PAYMENT = SMS_PAYMENT;
    }

    public boolean isCREATE_ACCOUNT_UNDER_18() {
        return CREATE_ACCOUNT_UNDER_18;
    }

    public void setCREATE_ACCOUNT_UNDER_18(boolean CREATE_ACCOUNT_UNDER_18) {
        this.CREATE_ACCOUNT_UNDER_18 = CREATE_ACCOUNT_UNDER_18;
    }

    public boolean isPAYMENT_RECEIPT() {
        return PAYMENT_RECEIPT;
    }

    public void setPAYMENT_RECEIPT(boolean PAYMENT_RECEIPT) {
        this.PAYMENT_RECEIPT = PAYMENT_RECEIPT;
    }

    public boolean isCONSENTS_TRIGGER() {
        return CONSENTS_TRIGGER;
    }

    public void setCONSENTS_TRIGGER(boolean CONSENTS_TRIGGER) {
        this.CONSENTS_TRIGGER = CONSENTS_TRIGGER;
    }

    public boolean isPENSION() {
        return PENSION;
    }

    public void setPENSION(boolean PENSION) {
        this.PENSION = PENSION;
    }

    public boolean isCARDS() {
        return CARDS;
    }

    public void setCARDS(boolean CARDS) {
        this.CARDS = CARDS;
    }

    public boolean isAKSJESPAREKONTO_OVERVIEW_TRIGGER() {
        return AKSJESPAREKONTO_OVERVIEW_TRIGGER;
    }

    public void setAKSJESPAREKONTO_OVERVIEW_TRIGGER(boolean AKSJESPAREKONTO_OVERVIEW_TRIGGER) {
        this.AKSJESPAREKONTO_OVERVIEW_TRIGGER = AKSJESPAREKONTO_OVERVIEW_TRIGGER;
    }

    public boolean isUSE_BETALING_APP() {
        return USE_BETALING_APP;
    }

    public void setUSE_BETALING_APP(boolean USE_BETALING_APP) {
        this.USE_BETALING_APP = USE_BETALING_APP;
    }

    public boolean isAPP_CREATE_TOKEN() {
        return APP_CREATE_TOKEN;
    }

    public void setAPP_CREATE_TOKEN(boolean APP_CREATE_TOKEN) {
        this.APP_CREATE_TOKEN = APP_CREATE_TOKEN;
    }

    public boolean isCARD_SHOW_PIN() {
        return CARD_SHOW_PIN;
    }

    public void setCARD_SHOW_PIN(boolean CARD_SHOW_PIN) {
        this.CARD_SHOW_PIN = CARD_SHOW_PIN;
    }

    public boolean isYOUTH_INSURANCE_MY_OVERVIEW() {
        return YOUTH_INSURANCE_MY_OVERVIEW;
    }

    public void setYOUTH_INSURANCE_MY_OVERVIEW(boolean YOUTH_INSURANCE_MY_OVERVIEW) {
        this.YOUTH_INSURANCE_MY_OVERVIEW = YOUTH_INSURANCE_MY_OVERVIEW;
    }

    public boolean isCREDIT_LIMIT_TRIGGER() {
        return CREDIT_LIMIT_TRIGGER;
    }

    public void setCREDIT_LIMIT_TRIGGER(boolean CREDIT_LIMIT_TRIGGER) {
        this.CREDIT_LIMIT_TRIGGER = CREDIT_LIMIT_TRIGGER;
    }

    public boolean isAPP_LOGIN_FINGERPRINT_IOS() {
        return APP_LOGIN_FINGERPRINT_IOS;
    }

    public void setAPP_LOGIN_FINGERPRINT_IOS(boolean APP_LOGIN_FINGERPRINT_IOS) {
        this.APP_LOGIN_FINGERPRINT_IOS = APP_LOGIN_FINGERPRINT_IOS;
    }

    public boolean isAKSJESPAREKONTO_IN_PM_SPARING() {
        return AKSJESPAREKONTO_IN_PM_SPARING;
    }

    public void setAKSJESPAREKONTO_IN_PM_SPARING(boolean AKSJESPAREKONTO_IN_PM_SPARING) {
        this.AKSJESPAREKONTO_IN_PM_SPARING = AKSJESPAREKONTO_IN_PM_SPARING;
    }

    public boolean isTRAVEL_INSURANCE_MY_OVERVIEW() {
        return TRAVEL_INSURANCE_MY_OVERVIEW;
    }

    public void setTRAVEL_INSURANCE_MY_OVERVIEW(boolean TRAVEL_INSURANCE_MY_OVERVIEW) {
        this.TRAVEL_INSURANCE_MY_OVERVIEW = TRAVEL_INSURANCE_MY_OVERVIEW;
    }

    public boolean isSTRAKSBETALING() {
        return STRAKSBETALING;
    }

    public void setSTRAKSBETALING(boolean STRAKSBETALING) {
        this.STRAKSBETALING = STRAKSBETALING;
    }

    public boolean isCARD_BLOCK_CARD() {
        return CARD_BLOCK_CARD;
    }

    public void setCARD_BLOCK_CARD(boolean CARD_BLOCK_CARD) {
        this.CARD_BLOCK_CARD = CARD_BLOCK_CARD;
    }

    public boolean isTRAVEL_INSURANCE() {
        return TRAVEL_INSURANCE;
    }

    public void setTRAVEL_INSURANCE(boolean TRAVEL_INSURANCE) {
        this.TRAVEL_INSURANCE = TRAVEL_INSURANCE;
    }

    public boolean isNEW_CREDIT_CARD_APPLICATION_RESPONSIVE() {
        return NEW_CREDIT_CARD_APPLICATION_RESPONSIVE;
    }

    public void setNEW_CREDIT_CARD_APPLICATION_RESPONSIVE(boolean NEW_CREDIT_CARD_APPLICATION_RESPONSIVE) {
        this.NEW_CREDIT_CARD_APPLICATION_RESPONSIVE = NEW_CREDIT_CARD_APPLICATION_RESPONSIVE;
    }

    public boolean isINSURANCE_EXTERNAL() {
        return INSURANCE_EXTERNAL;
    }

    public void setINSURANCE_EXTERNAL(boolean INSURANCE_EXTERNAL) {
        this.INSURANCE_EXTERNAL = INSURANCE_EXTERNAL;
    }

    public boolean isCARD_SAVINGS() {
        return CARD_SAVINGS;
    }

    public void setCARD_SAVINGS(boolean CARD_SAVINGS) {
        this.CARD_SAVINGS = CARD_SAVINGS;
    }

    public boolean isAPP_LOGIN_SRP() {
        return APP_LOGIN_SRP;
    }

    public void setAPP_LOGIN_SRP(boolean APP_LOGIN_SRP) {
        this.APP_LOGIN_SRP = APP_LOGIN_SRP;
    }

    public boolean isCONSENTS_VIEW() {
        return CONSENTS_VIEW;
    }

    public void setCONSENTS_VIEW(boolean CONSENTS_VIEW) {
        this.CONSENTS_VIEW = CONSENTS_VIEW;
    }

    public boolean isCREDIT_LIMIT() {
        return CREDIT_LIMIT;
    }

    public void setCREDIT_LIMIT(boolean CREDIT_LIMIT) {
        this.CREDIT_LIMIT = CREDIT_LIMIT;
    }

    public boolean isAKSJESPAREKONTO() {
        return AKSJESPAREKONTO;
    }

    public void setAKSJESPAREKONTO(boolean AKSJESPAREKONTO) {
        this.AKSJESPAREKONTO = AKSJESPAREKONTO;
    }

    public boolean isABOUT_ME_SETTINGS() {
        return ABOUT_ME_SETTINGS;
    }

    public void setABOUT_ME_SETTINGS(boolean ABOUT_ME_SETTINGS) {
        this.ABOUT_ME_SETTINGS = ABOUT_ME_SETTINGS;
    }

    public boolean isAKSJESPAREKONTO_ADVERTISEMENT() {
        return AKSJESPAREKONTO_ADVERTISEMENT;
    }

    public void setAKSJESPAREKONTO_ADVERTISEMENT(boolean AKSJESPAREKONTO_ADVERTISEMENT) {
        this.AKSJESPAREKONTO_ADVERTISEMENT = AKSJESPAREKONTO_ADVERTISEMENT;
    }

    public boolean isYOUTH_INSURANCE_TRIGGER() {
        return YOUTH_INSURANCE_TRIGGER;
    }

    public void setYOUTH_INSURANCE_TRIGGER(boolean YOUTH_INSURANCE_TRIGGER) {
        this.YOUTH_INSURANCE_TRIGGER = YOUTH_INSURANCE_TRIGGER;
    }

    public boolean isCONSENTS_EDIT() {
        return CONSENTS_EDIT;
    }

    public void setCONSENTS_EDIT(boolean CONSENTS_EDIT) {
        this.CONSENTS_EDIT = CONSENTS_EDIT;
    }

    public boolean isPENSION_TRIGGER() {
        return PENSION_TRIGGER;
    }

    public void setPENSION_TRIGGER(boolean PENSION_TRIGGER) {
        this.PENSION_TRIGGER = PENSION_TRIGGER;
    }

    public boolean isAPP_LOGIN_WEAK_TOKEN() {
        return APP_LOGIN_WEAK_TOKEN;
    }

    public void setAPP_LOGIN_WEAK_TOKEN(boolean APP_LOGIN_WEAK_TOKEN) {
        this.APP_LOGIN_WEAK_TOKEN = APP_LOGIN_WEAK_TOKEN;
    }

    public boolean isINSURANCE() {
        return INSURANCE;
    }

    public void setINSURANCE(boolean INSURANCE) {
        this.INSURANCE = INSURANCE;
    }

    public boolean isFUND_TRADING() {
        return FUND_TRADING;
    }

    public void setFUND_TRADING(boolean FUND_TRADING) {
        this.FUND_TRADING = FUND_TRADING;
    }

    public boolean isCHARGING() {
        return CHARGING;
    }

    public void setCHARGING(boolean CHARGING) {
        this.CHARGING = CHARGING;
    }

    public boolean isTRAVEL_INSURANCE_ORDER() {
        return TRAVEL_INSURANCE_ORDER;
    }

    public void setTRAVEL_INSURANCE_ORDER(boolean TRAVEL_INSURANCE_ORDER) {
        this.TRAVEL_INSURANCE_ORDER = TRAVEL_INSURANCE_ORDER;
    }

    public boolean isAPP_LOGIN_FINGERPRINT_ANDROID() {
        return APP_LOGIN_FINGERPRINT_ANDROID;
    }

    public void setAPP_LOGIN_FINGERPRINT_ANDROID(boolean APP_LOGIN_FINGERPRINT_ANDROID) {
        this.APP_LOGIN_FINGERPRINT_ANDROID = APP_LOGIN_FINGERPRINT_ANDROID;
    }

    public boolean isPAYMENT_TO_CREDIT_CARD() {
        return PAYMENT_TO_CREDIT_CARD;
    }

    public void setPAYMENT_TO_CREDIT_CARD(boolean PAYMENT_TO_CREDIT_CARD) {
        this.PAYMENT_TO_CREDIT_CARD = PAYMENT_TO_CREDIT_CARD;
    }

    public boolean isSMS_NOTIFICATION() {
        return SMS_NOTIFICATION;
    }

    public void setSMS_NOTIFICATION(boolean SMS_NOTIFICATION) {
        this.SMS_NOTIFICATION = SMS_NOTIFICATION;
    }

    public boolean isLoginEnabled() {
        return loginEnabled;
    }

    public void setLoginEnabled(boolean loginEnabled) {
        this.loginEnabled = loginEnabled;
    }

    public boolean isCONSENTS() {
        return CONSENTS;
    }

    public void setCONSENTS(boolean CONSENTS) {
        this.CONSENTS = CONSENTS;
    }

    public boolean isBECOME_CUSTOMER() {
        return BECOME_CUSTOMER;
    }

    public void setBECOME_CUSTOMER(boolean BECOME_CUSTOMER) {
        this.BECOME_CUSTOMER = BECOME_CUSTOMER;
    }

    public boolean isDIALOG() {
        return DIALOG;
    }

    public void setDIALOG(boolean DIALOG) {
        this.DIALOG = DIALOG;
    }

    public boolean isFUND() {
        return FUND;
    }

    public void setFUND(boolean FUND) {
        this.FUND = FUND;
    }

    public boolean isADOBE_CAMPAIGN() {
        return ADOBE_CAMPAIGN;
    }

    public void setADOBE_CAMPAIGN(boolean ADOBE_CAMPAIGN) {
        this.ADOBE_CAMPAIGN = ADOBE_CAMPAIGN;
    }

    public boolean isAPP_DOWNLOAD_TRAVELINSURANCE() {
        return APP_DOWNLOAD_TRAVELINSURANCE;
    }

    public void setAPP_DOWNLOAD_TRAVELINSURANCE(boolean APP_DOWNLOAD_TRAVELINSURANCE) {
        this.APP_DOWNLOAD_TRAVELINSURANCE = APP_DOWNLOAD_TRAVELINSURANCE;
    }

    public boolean isPAYMENT_INSURANCE() {
        return PAYMENT_INSURANCE;
    }

    public void setPAYMENT_INSURANCE(boolean PAYMENT_INSURANCE) {
        this.PAYMENT_INSURANCE = PAYMENT_INSURANCE;
    }

    public boolean isUSE_IFRAME_TRAVEL_INSURANCE() {
        return USE_IFRAME_TRAVEL_INSURANCE;
    }

    public void setUSE_IFRAME_TRAVEL_INSURANCE(boolean USE_IFRAME_TRAVEL_INSURANCE) {
        this.USE_IFRAME_TRAVEL_INSURANCE = USE_IFRAME_TRAVEL_INSURANCE;
    }

    public boolean isCAR_INSURANCE_MY_OVERVIEW() {
        return CAR_INSURANCE_MY_OVERVIEW;
    }

    public void setCAR_INSURANCE_MY_OVERVIEW(boolean CAR_INSURANCE_MY_OVERVIEW) {
        this.CAR_INSURANCE_MY_OVERVIEW = CAR_INSURANCE_MY_OVERVIEW;
    }

    public boolean isPENSION_MY_OVERVIEW() {
        return PENSION_MY_OVERVIEW;
    }

    public void setPENSION_MY_OVERVIEW(boolean PENSION_MY_OVERVIEW) {
        this.PENSION_MY_OVERVIEW = PENSION_MY_OVERVIEW;
    }

    public boolean isSAVINGS() {
        return SAVINGS;
    }

    public void setSAVINGS(boolean SAVINGS) {
        this.SAVINGS = SAVINGS;
    }

    public boolean isSAVINGS_TRIGGER() {
        return SAVINGS_TRIGGER;
    }

    public void setSAVINGS_TRIGGER(boolean SAVINGS_TRIGGER) {
        this.SAVINGS_TRIGGER = SAVINGS_TRIGGER;
    }

    public boolean isTRAVEL_INSURANCE_TRIGGER() {
        return TRAVEL_INSURANCE_TRIGGER;
    }

    public void setTRAVEL_INSURANCE_TRIGGER(boolean TRAVEL_INSURANCE_TRIGGER) {
        this.TRAVEL_INSURANCE_TRIGGER = TRAVEL_INSURANCE_TRIGGER;
    }

    public boolean isREGIONAL_BLOCKING() {
        return REGIONAL_BLOCKING;
    }

    public void setREGIONAL_BLOCKING(boolean REGIONAL_BLOCKING) {
        this.REGIONAL_BLOCKING = REGIONAL_BLOCKING;
    }

    public boolean isCREATE_ACCOUNT() {
        return CREATE_ACCOUNT;
    }

    public void setCREATE_ACCOUNT(boolean CREATE_ACCOUNT) {
        this.CREATE_ACCOUNT = CREATE_ACCOUNT;
    }

    public boolean isPAYMENT_RENDER_CID_BY_DEFAULT() {
        return PAYMENT_RENDER_CID_BY_DEFAULT;
    }

    public void setPAYMENT_RENDER_CID_BY_DEFAULT(boolean PAYMENT_RENDER_CID_BY_DEFAULT) {
        this.PAYMENT_RENDER_CID_BY_DEFAULT = PAYMENT_RENDER_CID_BY_DEFAULT;
    }

    public boolean isSUBMIT_FRAUD_CLAIM() {
        return SUBMIT_FRAUD_CLAIM;
    }

    public void setSUBMIT_FRAUD_CLAIM(boolean SUBMIT_FRAUD_CLAIM) {
        this.SUBMIT_FRAUD_CLAIM = SUBMIT_FRAUD_CLAIM;
    }
}
