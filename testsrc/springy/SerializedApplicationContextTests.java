package springy;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import springy.context.JRubyApplicationContext;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;

import com.sun.org.apache.xpath.internal.NodeSet;

/**
 * Tests XML serialization feature of JRubyApplicationContext.
 */
@Test
public class SerializedApplicationContextTests extends AbstractApplicationContextTests {

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
        ((ConfigurableApplicationContext) ctxt).close();
    }

    protected ApplicationContext createContext() throws Exception {
        //create a jruby context, serialize it to xml, and recreate context from xml
        JRubyApplicationContext jrubyCtxt = new JRubyApplicationContext(new ClassPathResource("/springy/context.rb"), false);

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
