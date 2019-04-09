package se.tink.backend.aggregation.agents.fraud.creditsafe.soap.xmlschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2001/XMLSchema}keybase">
 *       &lt;attribute name="refer" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "keyref")
public class Keyref extends Keybase {

    @XmlAttribute(name = "refer", required = true)
    protected QName refer;

    /**
     * Gets the value of the refer property.
     *
     * @return possible object is {@link QName }
     */
    public QName getRefer() {
        return refer;
    }

    /**
     * Sets the value of the refer property.
     *
     * @param value allowed object is {@link QName }
     */
    public void setRefer(QName value) {
        this.refer = value;
    }
}
