package se.tink.backend.aggregation.agents.fraud.creditsafe.soap.xmlschema;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Java class for keybase complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="keybase">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.w3.org/2001/XMLSchema}annotated">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2001/XMLSchema}selector"/>
 *         &lt;element ref="{http://www.w3.org/2001/XMLSchema}field" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "keybase",
        propOrder = {"selector", "field"})
@XmlSeeAlso({Keyref.class})
public class Keybase extends Annotated {

    @XmlElement(required = true)
    protected Selector selector;

    @XmlElement(required = true)
    protected List<Field> field;

    @XmlAttribute(name = "name", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String name;

    /**
     * Gets the value of the selector property.
     *
     * @return possible object is {@link Selector }
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Sets the value of the selector property.
     *
     * @param value allowed object is {@link Selector }
     */
    public void setSelector(Selector value) {
        this.selector = value;
    }

    /**
     * Gets the value of the field property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the field property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getField().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link Field }
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<Field>();
        }
        return this.field;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }
}
