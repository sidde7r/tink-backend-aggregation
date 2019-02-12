
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ChangesObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChangesObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ChangeCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EngText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SweText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ChangedDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="Pnr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangesObject", propOrder = {
    "changeCode",
    "engText",
    "sweText",
    "changedDate",
    "pnr"
})
public class ChangesObject {

    @XmlElement(name = "ChangeCode")
    protected String changeCode;
    @XmlElement(name = "EngText")
    protected String engText;
    @XmlElement(name = "SweText")
    protected String sweText;
    @XmlElement(name = "ChangedDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar changedDate;
    @XmlElement(name = "Pnr")
    protected String pnr;

    /**
     * Gets the value of the changeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangeCode() {
        return changeCode;
    }

    /**
     * Sets the value of the changeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangeCode(String value) {
        this.changeCode = value;
    }

    /**
     * Gets the value of the engText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngText() {
        return engText;
    }

    /**
     * Sets the value of the engText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngText(String value) {
        this.engText = value;
    }

    /**
     * Gets the value of the sweText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSweText() {
        return sweText;
    }

    /**
     * Sets the value of the sweText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSweText(String value) {
        this.sweText = value;
    }

    /**
     * Gets the value of the changedDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getChangedDate() {
        return changedDate;
    }

    /**
     * Sets the value of the changedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setChangedDate(XMLGregorianCalendar value) {
        this.changedDate = value;
    }

    /**
     * Gets the value of the pnr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPnr() {
        return pnr;
    }

    /**
     * Sets the value of the pnr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPnr(String value) {
        this.pnr = value;
    }

}
