package nl.opensolutions.xpathbinding.model;

import nl.opensolutions.xpathbinding.XPathBinding;

@XPathBinding(path = "/person")
public class Person {

    @XPathBinding(path = "/name/firstname")
    private String firstName;

    @XPathBinding(path = "/name/lastname")
    private String lastname;

    @XPathBinding(path = "/name/initials")
    private String initials;

    @XPathBinding(path = "/address")
    private Address address;

    public String getFirstName() {
        return firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public String getInitials() {
        return initials;
    }

    public Address getAddress() {
        return address;
    }
}
