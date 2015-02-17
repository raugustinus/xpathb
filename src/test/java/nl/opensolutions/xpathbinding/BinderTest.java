package nl.opensolutions.xpathbinding;

import nl.opensolutions.xpathbinding.model.Person;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.InputStream;

import static org.slf4j.LoggerFactory.getLogger;

public class BinderTest {

    private static final Logger LOG = getLogger(BinderTest.class);

    @Test
    public void shouldUnmarshallPerson() throws Exception {
        InputStream xmlStream = this.getClass().getResourceAsStream("/sample-xmls/sample-1.xml");
        Binder<Person> binder = new Binder<>();
        Person person = binder.marshall(xmlStream, Person.class);
        LOG.debug("person: {}", ReflectionToStringBuilder.toString(person));
    }
}