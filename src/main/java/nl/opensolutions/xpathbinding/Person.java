package nl.opensolutions.xpathbinding;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "person")
public class Person {

    @XPathBinding(path = "/person/name/firstname")
    private String firstName;

    @XPathBinding(path = "/person/name/lastname")
    private String lastname;

    @XPathBinding(path = "/person/name/initials")
    private String initials;

    @XPathBinding(path = "/person/address")
    private Address address;

}
