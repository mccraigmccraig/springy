package springy;

import static org.testng.Assert.assertEquals;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import org.jruby.Ruby;
import springy.beans.Bean1;
import springy.beans.Bean4;
import springy.context.RuntimeSpringyContext;

import java.util.Map;

/**
 * JRubyApplicationContextTests specific tests.
 */
@Test
public class SpringyContextTests extends AbstractContextTests {

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

  /** create from a file */
  protected ConfigurableApplicationContext createContext() throws Exception {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(),
                new ClassPathResource("springy/context.rb"));
    }

  /** create directly from a string */
  protected ConfigurableApplicationContext createContext( String context ) throws Exception {
      return new RuntimeSpringyContext( Ruby.getDefaultInstance() , context );
    }

  public void testInlineXml() {
        Bean1 bean12 = (Bean1) ctxt.getBean("bean1-2");
        assert bean12 != null;
        Bean1 bean13 = (Bean1) ctxt.getBean("bean1-3");
        assert bean13 != null;
    }

    public void testRubyInitialiser() {
        Bean1 bean1 = (Bean1) ctxt.getBean("bean1");
        assertEquals( bean1.something , 22 );
    }

    public void testErrorWithEmptyListsInCtor() throws Exception {
        String context =
                "bean :bean1_emptylist_in_ctor, \"springy.beans.Bean4\" do |b|\n" +
                        "    b.new(\"name\", {}, [])\n" +
                        "end";

      ConfigurableApplicationContext ctxt = createContext(context);

        Bean4 b4 = (Bean4) ctxt.getBean("bean1_emptylist_in_ctor");
        assert b4.getMap().isEmpty();
        assert b4.getList().isEmpty();
    }

    public void testYaml() {
        Bean4 bean4 = (Bean4) ctxt.getBean("bean4_yaml");
        Map m = bean4.getMap();
        assert m != null;

        assertEquals(m.size(), 2);
        assertEquals(m.get("key1"), "value1");
        assertEquals(m.get("key2"), ctxt.getBean("bean1"));
    }

    @Test(expectedExceptions = BeanDefinitionParsingException.class)
    public void testEnforceInit() throws Exception {
        String context = "bean :bean1, \"springy.beans.Bean1\", :init_method=\"jjlkjkl\"";
        ConfigurableApplicationContext ctxt = createContext(context);
    }

    public void testBeanWithoutBlock() throws Exception {
        String context = "bean :a_bean, \"springy.beans.Bean1\"";
        ConfigurableApplicationContext ctxt = createContext(context);
    }

    public void testResourceExists() throws Exception {
        String context = "bean :bean1, 'springy.beans.Bean1' if resource_exists?('/springy/a_map.yml')";
      ConfigurableApplicationContext ctxt = createContext(context);
        assert ctxt.getBean("bean1") instanceof Bean1;
    }

    public void testBeforeInitCalled() {
        Bean1 b = (Bean1) ctxt.getBean("bean1");
        assert b.beforeInitCalled;
    }
}
