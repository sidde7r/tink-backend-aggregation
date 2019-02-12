
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfEmailFilterObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfEmailFilterObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EmailFilterObject" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}EmailFilterObject" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfEmailFilterObject", propOrder = {
    "emailFilterObject"
})
public class ArrayOfEmailFilterObject {

    @XmlElement(name = "EmailFilterObject", nillable = true)
    protected List<EmailFilterObject> emailFilterObject;

    /**
     * Gets the value of the emailFilterObject property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the emailFilterObject property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEmailFilterObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EmailFilterObject }
     * 
     * 
     */
    public List<EmailFilterObject> getEmailFilterObject() {
        if (emailFilterObject == null) {
            emailFilterObject = new ArrayList<EmailFilterObject>();
        }
        return this.emailFilterObject;
    }

}
