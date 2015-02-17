package nl.opensolutions.xpathbinding;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "address")
public class Address {

//    @XPath(path = "/howto/topic[@name='PowerBuilder']/url[2]/text()")
    @XPathBinding(path = "/person/address/street1")
    private String street1;

    @XPathBinding(path = "/person/address/street2")
    private String street2;

    @XPathBinding(path = "/person/address/street3")
    private String street3;

    @XPathBinding(path = "/person/address/city")
    private String city;

    @XPathBinding(path = "/person/address/country")
    private String country;

}
