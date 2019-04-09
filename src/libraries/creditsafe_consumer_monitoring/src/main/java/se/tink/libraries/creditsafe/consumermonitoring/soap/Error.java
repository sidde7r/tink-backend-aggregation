package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Error complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Error">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Reject_Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Reject_Text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "Error",
        propOrder = {"rejectCode", "rejectText"})
public class Error {

    @XmlElement(name = "Reject_Code")
    protected String rejectCode;

    @XmlElement(name = "Reject_Text")
    protected String rejectText;

    /**
     * Gets the value of the rejectCode property.
     *
     * @return possible object is {@link String }
     */
    public String getRejectCode() {
        return rejectCode;
    }

    /**
     * Sets the value of the rejectCode property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRejectCode(String value) {
        this.rejectCode = value;
    }

    /**
     * Gets the value of the rejectText property.
     *
     * @return possible object is {@link String }
     */
    public String getRejectText() {
        return rejectText;
    }

    /**
     * Sets the value of the rejectText property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRejectText(String value) {
        this.rejectText = value;
    }
}
