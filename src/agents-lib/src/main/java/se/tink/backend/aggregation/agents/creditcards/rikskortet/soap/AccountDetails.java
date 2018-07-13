
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AccountDetails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AccountDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ErrorCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Balance" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="BalanceStr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CardStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CardEndValid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccountDetails", propOrder = {
    "errorCode",
    "balance",
    "balanceStr",
    "cardStatus",
    "cardEndValid"
})
public class AccountDetails {

    @XmlElement(name = "ErrorCode")
    protected int errorCode;
    @XmlElement(name = "Balance", required = true)
    protected BigDecimal balance;
    @XmlElement(name = "BalanceStr")
    protected String balanceStr;
    @XmlElement(name = "CardStatus")
    protected String cardStatus;
    @XmlElement(name = "CardEndValid")
    protected String cardEndValid;

    /**
     * Gets the value of the errorCode property.
     * 
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     */
    public void setErrorCode(int value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the balance property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Sets the value of the balance property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setBalance(BigDecimal value) {
        this.balance = value;
    }

    /**
     * Gets the value of the balanceStr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBalanceStr() {
        return balanceStr;
    }

    /**
     * Sets the value of the balanceStr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBalanceStr(String value) {
        this.balanceStr = value;
    }

    /**
     * Gets the value of the cardStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardStatus() {
        return cardStatus;
    }

    /**
     * Sets the value of the cardStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardStatus(String value) {
        this.cardStatus = value;
    }

    /**
     * Gets the value of the cardEndValid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardEndValid() {
        return cardEndValid;
    }

    /**
     * Sets the value of the cardEndValid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardEndValid(String value) {
        this.cardEndValid = value;
    }

}
