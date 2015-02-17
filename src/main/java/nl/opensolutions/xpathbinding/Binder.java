package nl.opensolutions.xpathbinding;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
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
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;
import static org.slf4j.LoggerFactory.getLogger;

public class Binder<T> {

    private static final Logger LOG = getLogger(Binder.class);


    public T marshall(InputStream xmlStream, Class<T> clazz) {
        Document doc = getDoc(xmlStream);
        try {
            T result = clazz.newInstance();
            return bind(doc, result);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BindingException("Unable to bind..", e);
        }
    }

    private <X> X bind(Node node, Object object) {
        LOG.debug("binding: {} for: {}", node, object.getClass().getSimpleName());
        try {

            Class<T> clazz = (Class<T>) object.getClass();
            for (Field field : clazz.getDeclaredFields()) {

                field.setAccessible(true);

                if (field.isAnnotationPresent(XPathBinding.class)) {

                    Class<?> fieldType = field.getType();

                    XPathBinding xPathExpression = field.getAnnotation(XPathBinding.class);
                    String xpathExpression = xPathExpression.path();
                    LOG.debug("xpath expression to fetch: {}", xpathExpression);

//                    switchFieldType(fieldType);

                    if (fieldType == String.class) {

                        field.set(object, readString(node, xpathExpression));

                    } else {

                        Node childNode = readNode(node, xpathExpression);
                        Object child = fieldType.newInstance();
                        Object boundChild = bind(childNode, child);
                        LOG.debug("boundchild: {}", ReflectionToStringBuilder.toString(child));
                        field.set(object, boundChild);
                    }
                }
            }

            return (X) object;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new BindingException("Error binding xml document", e);
        }
    }

    private void switchFieldType(Class<?> fieldType) {
        switch (fieldType.getName()) {
            case "java.lang.String":
                LOG.debug("yes switched a String");
                break;
            default:
                LOG.warn("Uknown fieldtype: {}", fieldType.getName());
        }
    }

    private Document getDoc(InputStream xmlStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlStream);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BindingException("Unable to read document", e);
        }
    }

    private String readString(Node node, String expression) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile(expression);
            return (String) expr.evaluate(node, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new BindingException("Error binding xpath expression: " + expression, e);
        }
    }

    private Node readNode(Node node, String expression) {
        notNull(node);
        notBlank(expression);

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        LOG.debug("finding: {} in: {}", expression, node.getNodeName());
        try {

            XPathExpression expr = xpath.compile(expression);
            return (Node) expr.evaluate(node, XPathConstants.NODE);

        } catch (XPathExpressionException e) {
            throw new BindingException(format("Unable to find node for expression: %s.", expression), e);
        }
    }

    private <Z> Z readObject(Node node, String expression, Class<Z> clazz) {

        notNull(node);
        notBlank(expression);

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        LOG.debug("finding: {} in: {}", node.getNodeName(), expression);
        Z instance;
        try {

            XPathExpression expr = xpath.compile(expression);
            Node nodeEval = (Node) expr.evaluate(node, XPathConstants.NODE);
            LOG.debug("eval: {} for expression: {}", nodeEval, expression);

            instance = clazz.newInstance();

        } catch (XPathExpressionException | InstantiationException | IllegalAccessException e) {
            throw new BindingException(format("Unable to bind xpath expression result: %s.", expression), e);
        }

        return instance;
    }

}
