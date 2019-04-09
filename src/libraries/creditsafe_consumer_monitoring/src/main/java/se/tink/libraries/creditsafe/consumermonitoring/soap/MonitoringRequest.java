package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for MonitoringRequest complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="MonitoringRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Account" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}Account" minOccurs="0"/>
 *         &lt;element name="Language" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}LANGUAGE"/>
 *         &lt;element name="ResultCounters" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ResultCountersReqObject" minOccurs="0"/>
 *         &lt;element name="PortfolioName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FromPortfolioName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Pnr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CustomerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FreeText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CommonText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="NumberOfDays" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ignoreFilter" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="All" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="TransactionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="MonitoredObjects" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfAnyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "MonitoringRequest",
        propOrder = {
            "account",
            "language",
            "resultCounters",
            "portfolioName",
            "fromPortfolioName",
            "pnr",
            "customerNo",
            "freeText",
            "commonText",
            "numberOfDays",
            "ignoreFilter",
            "all",
            "transactionId",
            "monitoredObjects"
        })
public class MonitoringRequest {

    @XmlElement(name = "Account")
    protected Account account;

    @XmlElement(name = "Language", required = true)
    protected LANGUAGE language;

    @XmlElement(name = "ResultCounters")
    protected ResultCountersReqObject resultCounters;

    @XmlElement(name = "PortfolioName")
    protected String portfolioName;

    @XmlElement(name = "FromPortfolioName")
    protected String fromPortfolioName;

    @XmlElement(name = "Pnr")
    protected String pnr;

    @XmlElement(name = "CustomerNo")
    protected String customerNo;

    @XmlElement(name = "FreeText")
    protected String freeText;

    @XmlElement(name = "CommonText")
    protected String commonText;

    @XmlElement(name = "NumberOfDays")
    protected int numberOfDays;

    protected boolean ignoreFilter;

    @XmlElement(name = "All")
    protected boolean all;

    @XmlElement(name = "TransactionId")
    protected String transactionId;

    @XmlElement(name = "MonitoredObjects")
    protected ArrayOfAnyType monitoredObjects;

    /**
     * Gets the value of the account property.
     *
     * @return possible object is {@link Account }
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the value of the account property.
     *
     * @param value allowed object is {@link Account }
     */
    public void setAccount(Account value) {
        this.account = value;
    }

    /**
     * Gets the value of the language property.
     *
     * @return possible object is {@link LANGUAGE }
     */
    public LANGUAGE getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is {@link LANGUAGE }
     */
    public void setLanguage(LANGUAGE value) {
        this.language = value;
    }

    /**
     * Gets the value of the resultCounters property.
     *
     * @return possible object is {@link ResultCountersReqObject }
     */
    public ResultCountersReqObject getResultCounters() {
        return resultCounters;
    }

    /**
     * Sets the value of the resultCounters property.
     *
     * @param value allowed object is {@link ResultCountersReqObject }
     */
    public void setResultCounters(ResultCountersReqObject value) {
        this.resultCounters = value;
    }

    /**
     * Gets the value of the portfolioName property.
     *
     * @return possible object is {@link String }
     */
    public String getPortfolioName() {
        return portfolioName;
    }

    /**
     * Sets the value of the portfolioName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPortfolioName(String value) {
        this.portfolioName = value;
    }

    /**
     * Gets the value of the fromPortfolioName property.
     *
     * @return possible object is {@link String }
     */
    public String getFromPortfolioName() {
        return fromPortfolioName;
    }

    /**
     * Sets the value of the fromPortfolioName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFromPortfolioName(String value) {
        this.fromPortfolioName = value;
    }

    /**
     * Gets the value of the pnr property.
     *
     * @return possible object is {@link String }
     */
    public String getPnr() {
        return pnr;
    }

    /**
     * Sets the value of the pnr property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPnr(String value) {
        this.pnr = value;
    }

    /**
     * Gets the value of the customerNo property.
     *
     * @return possible object is {@link String }
     */
    public String getCustomerNo() {
        return customerNo;
    }

    /**
     * Sets the value of the customerNo property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCustomerNo(String value) {
        this.customerNo = value;
    }

    /**
     * Gets the value of the freeText property.
     *
     * @return possible object is {@link String }
     */
    public String getFreeText() {
        return freeText;
    }

    /**
     * Sets the value of the freeText property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFreeText(String value) {
        this.freeText = value;
    }

    /**
     * Gets the value of the commonText property.
     *
     * @return possible object is {@link String }
     */
    public String getCommonText() {
        return commonText;
    }

    /**
     * Sets the value of the commonText property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCommonText(String value) {
        this.commonText = value;
    }

    /** Gets the value of the numberOfDays property. */
    public int getNumberOfDays() {
        return numberOfDays;
    }

    /** Sets the value of the numberOfDays property. */
    public void setNumberOfDays(int value) {
        this.numberOfDays = value;
    }

    /** Gets the value of the ignoreFilter property. */
    public boolean isIgnoreFilter() {
        return ignoreFilter;
    }

    /** Sets the value of the ignoreFilter property. */
    public void setIgnoreFilter(boolean value) {
        this.ignoreFilter = value;
    }

    /** Gets the value of the all property. */
    public boolean isAll() {
        return all;
    }

    /** Sets the value of the all property. */
    public void setAll(boolean value) {
        this.all = value;
    }

    /**
     * Gets the value of the transactionId property.
     *
     * @return possible object is {@link String }
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the value of the transactionId property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    /**
     * Gets the value of the monitoredObjects property.
     *
     * @return possible object is {@link ArrayOfAnyType }
     */
    public ArrayOfAnyType getMonitoredObjects() {
        return monitoredObjects;
    }

    /**
     * Sets the value of the monitoredObjects property.
     *
     * @param value allowed object is {@link ArrayOfAnyType }
     */
    public void setMonitoredObjects(ArrayOfAnyType value) {
        this.monitoredObjects = value;
    }
}
