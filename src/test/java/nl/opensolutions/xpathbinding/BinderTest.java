package nl.opensolutions.xpathbinding;

import nl.opensolutions.xpathbinding.model.Address;
import nl.opensolutions.xpathbinding.model.Person;
import nl.opensolutions.xpathbinding.stuf.NatuurlijkPersoon;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

public class BinderTest {

    private static final Logger LOG = getLogger(BinderTest.class);

    @Test
    public void shouldUnmarshallPerson() throws Exception {
        InputStream xmlStream = resourceAsStream("/sample-xmls/sample-1.xml");
        Binder<Person> binder = new Binder<>();
        Person person = binder.marshall(xmlStream, Person.class);
        LOG.debug("person: {}", ReflectionToStringBuilder.toString(person));
        Address address = person.getAddress();
        LOG.debug(" with address: {}", ReflectionToStringBuilder.toString(address));
    }

    @Test
    public void shouldUnmarshallNatuurlijkPersoon() throws Exception {
        InputStream xmlStream = resourceAsStream("/sample-xmls/npsLa01.xml");
        Binder<NatuurlijkPersoon> binder = new Binder<>();
        NatuurlijkPersoon nps = binder.marshall(xmlStream, NatuurlijkPersoon.class);
        LOG.debug("nps: {}", ReflectionToStringBuilder.toString(nps));
    }

    private InputStream resourceAsStream(String name) {
        return this.getClass().getResourceAsStream(name);
    }
}