package springy;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import springy.context.SpringyApplicationContext;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Tests XML serialization feature of SpringyApplicationContext.
 */
public abstract class AbstractContextSerializationTests extends AbstractContextTests {

    private String serializedContext;
    private Document serializedContextAsDoc;

    @BeforeClass
    public void beforeClass() throws Exception {
        //long start = System.currentTimeMillis();
        ctxt = createContext();
        //System.err.println("context created in " + (System.currentTimeMillis() - start) + " ms");
    }

    @AfterClass
    public void afterClass() throws Exception {
        ctxt.close();
    }

    protected Resource getContextResource() {
        return new ClassPathResource("springy/context.rb");
    }

    protected abstract SpringyApplicationContext doCreateContext();

    protected ConfigurableApplicationContext createContext() throws Exception {
        //create a jruby context, serialize it to xml, and recreate context from xml
        SpringyApplicationContext jrubyCtxt = doCreateContext();
        serializedContext = jrubyCtxt.getContextAsXml();
        serializedContextAsDoc = jrubyCtxt.getContextAsDocument();
        return new StringApplicationContext(serializedContext);
    }

    public void testDocument() throws Exception {
        assert serializedContextAsDoc != null;
        NodeList bean = serializedContextAsDoc.getElementsByTagName("bean");
        assertEquals(bean.getLength(), 13);


        assertEquals(serializedContextAsDoc.getChildNodes().getLength(), 2);
        // 12 toplevel bean definitions
        assertEquals(serializedContextAsDoc.getChildNodes().item(1).getChildNodes().getLength(), 12);
    }
    
    public void testBeanAttributesProperlySerialized() throws Exception {
        assertAttribute("bean5", "scope", "prototype");
        assertAttribute("bean5", "init-method", "myInit");
        assertAttribute("bean5", "destroy-method", "myDestroy");
        assertAttribute("bean5", "dependency-check", "simple");
        assertAttribute("bean5", "lazy-init", "true");
        assertAttribute("bean5", "abstract", "true");
        assertAttribute("bean5", "autowire", "byType");
    }

    private void assertAttribute(String id, String name, String value) throws Exception {
        Element e = getBeanById(id);
        assertEquals(e.getAttribute(name), value);
    }

    private Element getBeanById(String id) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList nodes = (NodeList) xpath.evaluate("//bean[@id='" + id + "']", serializedContextAsDoc, XPathConstants.NODESET);
        assert nodes != null;
        assertEquals(nodes.getLength(), 1);
        return (Element) nodes.item(0);
    }

    static class StringApplicationContext extends AbstractXmlApplicationContext {
        private String contextAsString;

        public StringApplicationContext(String s) {
            this.contextAsString = s;
            refresh();
        }

        protected Resource[] getConfigResources() {
            Resource[] r = new Resource[1];
            r[0] = new ByteArrayResource(contextAsString.getBytes(), "serialized context");
            return r;
        }
    }
}
