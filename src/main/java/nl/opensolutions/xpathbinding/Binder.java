package nl.opensolutions.xpathbinding;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;
import static org.slf4j.LoggerFactory.getLogger;

public class Binder<T> {

    private static final Logger LOG = getLogger(Binder.class);
    private Map<String, String> namespaces = new HashMap<>();

    public T marshall(InputStream xmlStream, Class<T> clazz) {

        Document document = getDoc(xmlStream);

        recurseNamespaces(document, namespaces);
        logNamespaces(namespaces);

        try {
            T result = clazz.newInstance();
            return bind(document, result);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BindingException("Unable to bind..", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <X> X bind(Node node, Object object) {

        LOG.debug("binding: {}", node.getNodeName());

        try {

            Class<T> clazz = (Class<T>) object.getClass();

            String rootPath = "";
            if(clazz.isAnnotationPresent(XPathBinding.class)) {
                rootPath = clazz.getAnnotation(XPathBinding.class).path();
            }

            for (Field field : clazz.getDeclaredFields()) {

                if (field.isAnnotationPresent(XPathBinding.class)) {

                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    XPathBinding xPathBinding = field.getAnnotation(XPathBinding.class);
                    String expression = rootPath + xPathBinding.path();

                    if (fieldType == String.class) {
                        String value = readString(node, expression);
                        LOG.debug("setting value: {} on: {}", value, object.getClass().getSimpleName());
                        field.set(object, value);
                    } else {
                        Node childNode = readNode(node, expression);
                        Object child = fieldType.newInstance();
                        Object boundChild = bind(childNode, child);
                        field.set(object, boundChild);
                    }
                }
            }

            return (X) object;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new BindingException("Error binding xml document", e);
        }
    }

    private Document getDoc(InputStream xmlStream) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // TODO: impl errorhandling
//        factory.setValidating(true);
//        ErrorHandler handler = new SAXValidator();
//        parser.setErrorHandler(handler);

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlStream);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new BindingException("Unable to read document", e);
        }
    }

    private void logNamespaces(Map<String, String> namespaces) {
        for(Map.Entry<String, String> entry : namespaces.entrySet()) {
            LOG.debug("namespace entry: {} - {}", entry.getKey(), entry.getValue());
        }
    }

    private void recurseNamespaces(Node node, Map<String, String> namespaces) {
        String prefix = node.getPrefix();
        if(isNotBlank(prefix)) {
            namespaces.put(prefix, node.getNamespaceURI());
        }
        NodeList childNodes = node.getChildNodes();
        for(int i=0;i<childNodes.getLength();i++) {
            recurseNamespaces(childNodes.item(i), namespaces);
        }
    }

    private NamespaceContext namespaceContext() {
        return new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                return namespaces.get(prefix);
            }
            @Override
            public Iterator getPrefixes(String val) {
                return namespaces.keySet().iterator();
            }
            @Override
            public String getPrefix(String uri) {
                throw new IllegalAccessError("Not implemented!");
            }
        };
    }

    private String readString(Node node, String expression) {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression expr = xpath.compile(expression);
            LOG.debug("expression: {}", expression);
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
        xpath.setNamespaceContext(namespaceContext());

        try {
            XPathExpression expr = xpath.compile(expression);
            return (Node) expr.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new BindingException(format("Unable to find node for expression: %s.", expression), e);
        }
    }

}
