package nl.opensolutions.xpathbinding.stuf;

import nl.opensolutions.xpathbinding.XPathBinding;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "")
public class NatuurlijkPersoon {

    @XPathBinding(path = "//*[contains(name(),\"bg310:inp.bsn\")]")
    private String bsn;

    @XPathBinding(path = "//*[contains(name(),\"bg310:geslachtsnaam\")]")
    private String geslachtsnaam;
}
