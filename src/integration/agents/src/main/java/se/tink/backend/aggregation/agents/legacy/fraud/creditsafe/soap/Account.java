package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Account complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Account">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UserName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TransactionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Language" type="{https://webservice.creditsafe.se/getdata/}LANGUAGE"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "Account",
        propOrder = {"userName", "password", "transactionId", "language"})
public class Account {

    @XmlElement(name = "UserName")
    protected String userName;

    @XmlElement(name = "Password")
    protected String password;

    @XmlElement(name = "TransactionId")
    protected String transactionId;

    @XmlElement(name = "Language", required = true)
    protected Language language;

    /**
     * Gets the value of the userName property.
     *
     * @return possible object is {@link String }
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the value of the userName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Gets the value of the password property.
     *
     * @return possible object is {@link String }
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     *
     * @param value allowed object is {@link String }
     */
    public void setPassword(String value) {
        this.password = value;
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
     * Gets the value of the language property.
     *
     * @return possible object is {@link Language }
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is {@link Language }
     */
    public void setLanguage(Language value) {
        this.language = value;
    }
}
