package propertymanager;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import xmlutils.InvalidXMLFileFormatException;
import xmlutils.XMLUtilities;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author Ritwik Banerjee
 */
public class PropertyManager {

    private static final XMLUtilities xmlUtilities = new XMLUtilities();

    private static PropertyManager singleton = null;

    private Map<String, String>       properties;
    private Map<String, List<String>> propertyOptions;

    // Constants critical to the loading of elements and their attributes from the XML files
    public static final String PROPERTY_ELEMENT              = "property";
    public static final String PROPERTY_LIST_ELEMENT         = "property_list";
    public static final String PROPERTY_OPTIONS_LIST_ELEMENT = "property_options_list";
    public static final String PROPERTY_OPTIONS_ELEMENT      = "property_options";
    public static final String OPTION_ELEMENT                = "option";
    public static final String NAME_ATTRIBUTE                = "name";
    public static final String VALUE_ATTRIBUTE               = "value";

    // Location of the properties resources, relative to the root resource folder for the application
    public static final String PROPERTIES_RESOURCE_RELATIVE_PATH = "properties";

    private PropertyManager() {
        properties = new HashMap<>();
        propertyOptions = new HashMap<>();
    }

    public static PropertyManager getManager() {
        if (singleton == null)
            singleton = new PropertyManager();
        return singleton;
    }

    public void addProperty(String property, String value) {
        properties.put(property, value);
    }

    public String getPropertyValue(String property) {
        return properties.get(property);
    }

    public String getPropertyValue(Object property) {
        return properties.get(property.toString());
    }

    public void addPropertyOption(String property, String option) {
        if (properties.get(property) == null)
            throw new NoSuchElementException(String.format("Property \"%s\" does not exist.", property));
        List<String> propertyoptionslist = propertyOptions.get(property);
        if (propertyoptionslist == null)
            propertyoptionslist = new ArrayList<>();
        propertyoptionslist.add(option);
        propertyOptions.put(property, propertyoptionslist);
    }
    
    public List<String> getPropertyOptions(String property) {
        if (properties.get(property) == null)
            throw new NoSuchElementException(String.format("Property \"%s\" does not exist.", property));
        return propertyOptions.get(property);
    }

    public boolean hasProperty(Object property) {
        return properties.get(property.toString()) != null;
    }
    
    public void loadProperties(Class klass, String xmlfilename, String schemafilename) throws InvalidXMLFileFormatException {
        URL             xmlFileResource    = klass.getClassLoader().getResource(PROPERTIES_RESOURCE_RELATIVE_PATH + File.separator + xmlfilename);
        URL             schemaFileResource = klass.getClassLoader().getResource(PROPERTIES_RESOURCE_RELATIVE_PATH + File.separator + schemafilename);
        Document        document           = xmlUtilities.loadXMLDocument(xmlFileResource, schemaFileResource);
        Node            propertyListNode   = xmlUtilities.getNodeWithName(document, PROPERTY_LIST_ELEMENT);
        ArrayList<Node> propNodes          = xmlUtilities.getChildNodesWithName(propertyListNode, PROPERTY_ELEMENT);
        for (Node n : propNodes) {
            NamedNodeMap attributes = n.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                String attName  = attributes.getNamedItem(NAME_ATTRIBUTE).getTextContent();
                String attValue = attributes.getNamedItem(VALUE_ATTRIBUTE).getTextContent();
                properties.put(attName, attValue);
            }
        }
        
        // AND THE PROPERTIES FROM OPTION LISTS
        Node propertyOptionsListNode = xmlUtilities.getNodeWithName(document, PROPERTY_OPTIONS_LIST_ELEMENT);
        if (propertyOptionsListNode != null) {
            ArrayList<Node> propertyOptionsNodes = xmlUtilities.getChildNodesWithName(propertyOptionsListNode, PROPERTY_OPTIONS_ELEMENT);
            for (Node n : propertyOptionsNodes) {
                NamedNodeMap      attributes = n.getAttributes();
                String            name       = attributes.getNamedItem(NAME_ATTRIBUTE).getNodeValue();
                ArrayList<String> options    = new ArrayList<>();
                propertyOptions.put(name, options);
                ArrayList<Node> optionsNodes = xmlUtilities.getChildNodesWithName(n, OPTION_ELEMENT);
                for (Node oNode : optionsNodes) {
                    String option = oNode.getTextContent();
                    options.add(option);
                }
            }
        }
    }
}
