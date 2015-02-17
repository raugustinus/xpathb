package nl.opensolutions.xpathbinding;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static java.lang.String.format;
import static org.apache.commons.lang3.exception.ExceptionUtils.getMessage;
import static org.slf4j.LoggerFactory.getLogger;

public class Binder<T> {

    private static final Logger LOG = getLogger(Binder.class);

    public T bind(InputStream xmlStream, Class<T> clazz) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException {

        T instance = null;
        Document doc = getDoc(xmlStream);

        try {

            instance = clazz.newInstance();

            for(Field field : clazz.getDeclaredFields()) {

                field.setAccessible(true);

//                LOG.debug("{} field: {} type: {}", field.isAnnotationPresent(XPathBinding.class), field.getName(), field.getType().getSimpleName());

                if(field.isAnnotationPresent(XPathBinding.class)) {

                    String fieldName = field.getName();
                    Class<?> fieldType = field.getType();

                    XPathBinding xPathExpression = field.getAnnotation(XPathBinding.class);
                    String xpathExpression = xPathExpression.path();
                    LOG.debug("xpath expression to fetch: {}", xpathExpression);


                    if(fieldType == String.class) {
                        field.set(instance, readString(doc, xpathExpression));
                    } else {

                        Object node = readObject(doc, xpathExpression, fieldType);
                        LOG.debug(format("xpathexpression: %s\n%s ", xpathExpression, node));
//                        field.set(instance, readObject(doc, xpathExpression));

                    }
                }
            }

        } catch (InstantiationException|IllegalAccessException e) {
            LOG.error(getMessage(e), e);
        }

        return instance;
    }

    private Document getDoc(InputStream xmlStream) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlStream);
    }

    private String readString(Document doc, String expression) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(expression);
        String eval = (String) expr.evaluate(doc, XPathConstants.STRING);
        return eval;
    }

    private <Z> Z readObject(Document doc, String expression, Class<Z> clazz) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        Z instance;
        try {

            XPathExpression expr = xpath.compile(expression);
            Node evaluate = (Node) expr.evaluate(doc, XPathConstants.NODE);
            LOG.debug("eval: {}", evaluate);

            instance = clazz.newInstance();

        } catch (XPathExpressionException|InstantiationException|IllegalAccessException e) {
            throw new BindingException(format("Unable to bind xpath expression result: %s.", expression), e);
        }

        return instance;
    }

}
