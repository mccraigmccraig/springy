package springy.context;

import org.w3c.dom.Document;

/**
 * Interface implemented by spring objects which can serialize themselves to XML.
 */
public interface XmlSerializable {
    void serialize_xml(Document doc);
}
