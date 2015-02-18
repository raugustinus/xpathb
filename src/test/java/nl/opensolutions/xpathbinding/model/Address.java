package nl.opensolutions.xpathbinding.model;

import nl.opensolutions.xpathbinding.XPathBinding;

@XPathBinding(path = "/person/address")
public class Address {

    @XPathBinding(path = "/street1")
    private String street1;

    @XPathBinding(path = "/street2")
    private String street2;

    @XPathBinding(path = "/street3")
    private String street3;

    @XPathBinding(path = "/city")
    private String city;

    @XPathBinding(path = "/country")
    private String country;

    public String getStreet1() {
        return street1;
    }

    public String getStreet2() {
        return street2;
    }

    public String getStreet3() {
        return street3;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }
}
