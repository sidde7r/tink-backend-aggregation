package se.tink.backend.aggregation.agents.fraud.creditsafe.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for ERROR complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ERROR">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Cause_of_Reject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Reject_text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Reject_comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ERROR",
        propOrder = {"causeOfReject", "rejectText", "rejectComment"})
public class Error {

    @XmlElement(name = "Cause_of_Reject")
    protected String causeOfReject;

    @XmlElement(name = "Reject_text")
    protected String rejectText;

    @XmlElement(name = "Reject_comment")
    protected String rejectComment;

    /**
     * Gets the value of the causeOfReject property.
     *
     * @return possible object is {@link String }
     */
    public String getCauseOfReject() {
        return causeOfReject;
    }

    /**
     * Sets the value of the causeOfReject property.
     *
     * @param value allowed object is {@link String }
     */
    public void setCauseOfReject(String value) {
        this.causeOfReject = value;
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

    /**
     * Gets the value of the rejectComment property.
     *
     * @return possible object is {@link String }
     */
    public String getRejectComment() {
        return rejectComment;
    }

    /**
     * Sets the value of the rejectComment property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRejectComment(String value) {
        this.rejectComment = value;
    }
}
