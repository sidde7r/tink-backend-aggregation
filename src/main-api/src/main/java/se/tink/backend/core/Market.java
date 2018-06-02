package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.swagger.annotations.ApiModelProperty;
import java.util.Collections;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.date.ResolutionTypes;

@Entity
@Table(name = "markets")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Market implements Cloneable {

    public enum Code {
        AT,
        AU,
        BE,
        BG,
        BR,
        CA,
        CY,
        CZ,
        DE,
        DK,
        EE,
        ES,
        FI,
        FR,
        GB,
        GR,
        HR,
        HU,
        IE,
        IN,
        IT,
        LU,
        LV,
        MT,
        NL,
        NO,
        NZ,
        PL,
        PT,
        RO,
        SE,
        SG,
        SI,
        SK,
        US
    }

    @Exclude
    @ApiModelProperty(name = "addressEmail", hidden = true)
    private String addressEmail;
    @Exclude
    @ApiModelProperty(name = "addressFacebook", hidden = true)
    private String addressFacebook;
    @Exclude
    @ApiModelProperty(name = "addressTwitter", hidden = true)
    private String addressTwitter;
    @Exclude
    @ApiModelProperty(name = "addressWeb", hidden = true)
    private String addressWeb;
    @Tag(1)
    @Id
    private String code;
    @Tag(2)
    @Transient
    private List<Currency> currencies;
    @Tag(3)
    private String defaultCurrency;
    @Tag(4)
    @ApiModelProperty(name = "defaultEndpoint", hidden = true)
    private String defaultEndpoint;
    @Tag(5)
    private String defaultLocale;
    @Tag(6)
    @JsonIgnore
    private boolean defaultMarket;
    @Tag(7)
    @JsonIgnore
    private int defaultPeriodBreak;
    @Tag(8)
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private ResolutionTypes defaultPeriodMode;
    @Tag(15)
    // defaultTimeZone is used on FE but doesnt exist on BE. FE has it as tag 15.
    private String defaultTimeZone;
    @Tag(9)
    private String description;
    @Tag(10)
    @ApiModelProperty(name = "linkToAboutPage", hidden = true)
    private String linkToAboutPage;
    @Tag(16)
    @ApiModelProperty(name = "linkToHelpPage", hidden = true)
    private String linkToHelpPage;
    @Tag(11)
    @ApiModelProperty(name = "linkToTermsOfServicePage", hidden = true)
    private String linkToTermsOfServicePage;
    @Tag(17)
    @ApiModelProperty(name = "linkToSecurityPage", hidden = true)
    private String linkToSecurityPage;
    @Exclude
    @ApiModelProperty(name = "organizationName", hidden = true)
    private String organizationName;
    @Tag(13)
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    private MarketStatus status;
    @Tag(14)
    @Transient
    private boolean suggested;
    @Tag(18)
    @ApiModelProperty(name = "linkToFraudPage", hidden = true)
    private String linkToFraudPage;
    @Tag(19)
    @ApiModelProperty(name = "linkToFraudTermsOfServicePage", hidden = true)
    private String linkToFraudTermsOfServicePage;
    @Tag(20)
    @ApiModelProperty(name = "linkToFraudPlusPage", hidden = true)
    private String linkToFraudPlusPage;
    @Tag(21)
    @ApiModelProperty(name = "linkToFraudPlusServiceTelephone", hidden = true)
    private String linkToFraudPlusServiceTelephone;
    @Tag(22)
    @ApiModelProperty(name = "linkToFraudPlusCardBlockPage", hidden = true)
    private String linkToFraudPlusCardBlockPage;
    @Tag(23)
    @ApiModelProperty(name = "linkToFraudPlusTermsOfServicePage", hidden = true)
    private String linkToFraudPlusTermsOfServicePage;
    @Tag(24)
    @ApiModelProperty(name = "registerMethods")
    @Transient
    private List<AuthenticationMethod> registerMethods;
    @Tag(25)
    @ApiModelProperty(name = "loginMethods")
    @Transient
    private List<AuthenticationMethod> loginMethods;
    @Tag(26)
    @ApiModelProperty(name = "gdprLoginMethods")
    @Transient
    private List<AuthenticationMethod> gdprLoginMethods;

    public Market() {

    }

    public Market(String code, String defaultLocale) {
        this.code = code;
        this.defaultLocale = defaultLocale;
    }

    public Market(String code, String description, String defaultLocale, String defaultCurrency, MarketStatus status,
            String linkToAboutPage, String linkToHelpPage, String linkToTermsOfServicePage, String defaultEndpoint,
            ResolutionTypes defaultPeriodMode, int defaultPeriodBreak, boolean defaultMarket, String defaultTimeZone,
            String organizationName, String addressEmail, String addressFacebook, String addressTwitter,
            String addressWeb, String linkToSecurityPage, String linkToFraudTermsOfServicePage,
            String linkToFraudPlusPage, String linkToFraudPlusServiceTelephone, String linkToFraudPlusCardBlockPage,
            List<AuthenticationMethod> registerMethods, List<AuthenticationMethod> loginMethods) {
        this.code = code;
        this.description = description;
        this.defaultLocale = defaultLocale;
        this.defaultCurrency = defaultCurrency;
        this.status = status;
        this.linkToAboutPage = linkToAboutPage;
        this.linkToHelpPage = linkToHelpPage;
        this.linkToTermsOfServicePage = linkToTermsOfServicePage;
        this.defaultEndpoint = defaultEndpoint;
        this.defaultPeriodMode = defaultPeriodMode;
        this.defaultPeriodBreak = defaultPeriodBreak;
        this.defaultMarket = defaultMarket;
        this.defaultTimeZone = defaultTimeZone;
        this.organizationName = organizationName;
        this.addressEmail = addressEmail;
        this.addressFacebook = addressFacebook;
        this.addressTwitter = addressTwitter;
        this.addressWeb = addressWeb;
        this.linkToSecurityPage = linkToSecurityPage;
        this.linkToFraudTermsOfServicePage = linkToFraudTermsOfServicePage;

        // Conditionals below are used while we have markets with undefined login/register methods

        if (registerMethods == null) {
            this.registerMethods = Collections.emptyList();
        } else {
            this.registerMethods = registerMethods;
        }

        if (loginMethods == null) {
            this.loginMethods = Collections.emptyList();
        } else {
            this.loginMethods = loginMethods;
        }

        this.setLinkToFraudPlusPage(linkToFraudPlusPage);
        this.setLinkToFraudPlusServiceTelephone(linkToFraudPlusServiceTelephone);
        this.setLinkToFraudPlusCardBlockPage(linkToFraudPlusCardBlockPage);
    }

    @Override
    public Market clone() {
        try {
            return (Market) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getAddressEmail() {
        return addressEmail;
    }

    public String getAddressFacebook() {
        return addressFacebook;
    }

    public String getAddressTwitter() {
        return addressTwitter;
    }

    public String getAddressWeb() {
        return addressWeb;
    }

    @ApiModelProperty(name = "codeAsString", hidden = true)
    public String getCodeAsString() {
        return code;
    }

    @ApiModelProperty(name = "code", value="The ISO 3166-1 alpha-2 country code of the market.", example="SE", required = true)
    public Code getCode() {
        return Code.valueOf(code);
    }

    @ApiModelProperty(name = "currencies", value="The applicable currencies available in the market.", required = true)
    public List<Currency> getCurrencies() {
        return currencies;
    }

    @ApiModelProperty(name = "defaultCurrency", value="The ISO 4217 code of the default currency.", example="SEK", required = true)
    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getDefaultEndpoint() {
        return defaultEndpoint;
    }

    @ApiModelProperty(name = "defaultLocale", value="The default locale in the market.", example="sv_SE", required = true)
    public String getDefaultLocale() {
        return defaultLocale;
    }

    @ApiModelProperty(name = "defaultPeriodBreak", value="The default period day break in the market.", example="25", required = true)
    public int getDefaultPeriodBreak() {
        return defaultPeriodBreak;
    }

    @ApiModelProperty(name = "defaultPeriodMode", value="The default period mode in the market.", allowableValues = ResolutionTypes.PERIOD_MODE_DOCUMENTED, required = true)
    public ResolutionTypes getDefaultPeriodMode() {
        return defaultPeriodMode;
    }

    @ApiModelProperty(name = "defaultTimeZone", value="The default time zone in the market.", example="Europe/Stockholm", required = true)
    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    @ApiModelProperty(name = "description", value="The display name of the market", example="Sweden", required = true)
    public String getDescription() {
        return description;
    }

    public String getLinkToAboutPage() {
        return linkToAboutPage;
    }

    public String getLinkToHelpPage() {
        return linkToHelpPage;
    }

    public String getLinkToTermsOfServicePage() {
        return linkToTermsOfServicePage;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    @ApiModelProperty(name = "status", value="The status of the market.", required = true)
    public MarketStatus getStatus() {
        return status;
    }

    @ApiModelProperty(name = "defaultMarket", value="Indicates if this is the default market.", required = true)
    public boolean isDefaultMarket() {
        return defaultMarket;
    }

    @ApiModelProperty(name = "suggested", value="Flag to indicate if this is the suggested market for the user.", required = true)
    public boolean isSuggested() {
        return suggested;
    }

    public void setAddressEmail(String addressEmail) {
        this.addressEmail = addressEmail;
    }

    public void setAddressFacebook(String addressFacebook) {
        this.addressFacebook = addressFacebook;
    }

    public void setAddressTwitter(String addressTwitter) {
        this.addressTwitter = addressTwitter;
    }

    public void setAddressWeb(String addressWeb) {
        this.addressWeb = addressWeb;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public void setDefaultEndpoint(String defaultEndpoint) {
        this.defaultEndpoint = defaultEndpoint;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public void setDefaultMarket(boolean defaultMarket) {
        this.defaultMarket = defaultMarket;
    }

    public void setDefaultPeriodBreak(int defaultPeriodBreak) {
        this.defaultPeriodBreak = defaultPeriodBreak;
    }

    public void setDefaultPeriodMode(ResolutionTypes defaultPeriodMode) {
        this.defaultPeriodMode = defaultPeriodMode;
    }

    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLinkToAboutPage(String linkToAboutPage) {
        this.linkToAboutPage = linkToAboutPage;
    }

    public void setLinkToHelpPage(String linkToHelpPage) {
        this.linkToHelpPage = linkToHelpPage;
    }

    public void setLinkToTermsOfServicePage(String linkToTermsOfServicePage) {
        this.linkToTermsOfServicePage = linkToTermsOfServicePage;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setStatus(MarketStatus status) {
        this.status = status;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }

    public String getLinkToSecurityPage() {
        return linkToSecurityPage;
    }

    public void setLinkToSecurityPage(String linkToSecurityPage) {
        this.linkToSecurityPage = linkToSecurityPage;
    }

    public String getLinkToFraudPage() {
        return linkToFraudPage;
    }

    public void setLinkToFraudPage(String linkToFraudPage) {
        this.linkToFraudPage = linkToFraudPage;
    }

    public String getLinkToFraudTermsOfServicePage() {
        return linkToFraudTermsOfServicePage;
    }

    public void setLinkToFraudTermsOfServicePage(String linkToFraudTermsOfServicePage) {
        this.linkToFraudTermsOfServicePage = linkToFraudTermsOfServicePage;
    }

    public String getLinkToFraudPlusPage() {
        return linkToFraudPlusPage;
    }

    public void setLinkToFraudPlusPage(String linkToFraudPlusPage) {
        this.linkToFraudPlusPage = linkToFraudPlusPage;
    }

    public String getLinkToFraudPlusServiceTelephone() {
        return linkToFraudPlusServiceTelephone;
    }

    public void setLinkToFraudPlusServiceTelephone(String linkToFraudPlusServiceTelephone) {
        this.linkToFraudPlusServiceTelephone = linkToFraudPlusServiceTelephone;
    }

    public String getLinkToFraudPlusCardBlockPage() {
        return linkToFraudPlusCardBlockPage;
    }

    public void setLinkToFraudPlusCardBlockPage(String linkToFraudPlusCardBlockPage) {
        this.linkToFraudPlusCardBlockPage = linkToFraudPlusCardBlockPage;
    }

    public String getLinkToFraudPlusTermsOfServicePage() {
        return linkToFraudPlusTermsOfServicePage;
    }

    public void setLinkToFraudPlusTermsOfServicePage(String linkToFraudPlusTermsOfServicePage) {
        this.linkToFraudPlusTermsOfServicePage = linkToFraudPlusTermsOfServicePage;
    }

    public List<AuthenticationMethod> getRegisterMethods() {
        return registerMethods;
    }

    public void setRegisterMethods(List<AuthenticationMethod> registerMethods) {
        this.registerMethods = registerMethods;
    }

    public List<AuthenticationMethod> getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(List<AuthenticationMethod> loginMethods) {
        this.loginMethods = loginMethods;
    }

    public void setGdprLoginMethods(List<AuthenticationMethod> gdprLoginMethods) {
        this.gdprLoginMethods = gdprLoginMethods;
    }

    public List<AuthenticationMethod> getGdprLoginMethods() {
        return gdprLoginMethods;
    }
}
