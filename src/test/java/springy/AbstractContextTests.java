package springy;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import org.springframework.context.ConfigurableApplicationContext;
import springy.beans.Bean1;
import springy.beans.Bean2;
import springy.beans.Bean3;
import springy.beans.Bean4;

import java.util.List;
import java.util.Arrays;

/**
 * General test conditions - implemented by XmlContext as well
 * as SpringyContext.
 */
@Test
public abstract class AbstractContextTests {
    protected ConfigurableApplicationContext ctxt;

    protected abstract ConfigurableApplicationContext createContext() throws Exception;

    public void testGetBeans() throws Exception {
        assert ctxt != null : getClass().getSimpleName();
        testBean1("bean1");
        testBean1("bean1-verbose");

        Bean2 b2 = (Bean2) ctxt.getBean("bean2");
        assert b2 != null;
        assert Bean1.staticMethodWasCalled();
    }

    private void testBean1(String name) {
        Bean1 b1 = (Bean1) ctxt.getBean(name);
        assert b1 != null;
        assert b1.getBean2() != null;
        assert b1.wasInitialised();
        assert b1.isBooleanProperty();
    }


    /*
    // why does this fail when run from maven?
    public void testStaticMethodWithParameters() {
        assertEquals(Bean1.sP1, 20);
        assertEquals(Bean1.sP2, "a String");
        assert Bean1.staticBeanRef instanceof Bean2;
    }
    */

    public void testGetList() {
        Bean1 b1 = (Bean1) ctxt.getBean("bean1");
        List l = b1.getListProperty();
        assert l != null;
        assert l.size() == 3;
        assertEquals(l, Arrays.asList("1", "2", "3"));
    }

    public void testCtor() {
        Bean2 b2 = (Bean2) ctxt.getBean("bean2");
        assertEquals(b2.getName1(), "Max");
        assertEquals(b2.getName2(), "Pierre");
        assertEquals(b2.getNumber(), 45);
    }

    public void testCtorRef() {
        Bean3 b3 = (Bean3) ctxt.getBean("bean3");
        assert b3.getBean2() != null;
        assertEquals(b3.getName(), "Vic");
    }

    public void testSetArguments() {
        Bean3 b3 = (Bean3) ctxt.getBean("bean3");
        Object[] args = b3.getParameters();

        assert args != null;
        assertEquals(args.length, 2, Arrays.asList(args).toString());
        assert args[0] instanceof Bean1 :Arrays.asList(args).toString();
        assertEquals(args[1], "A String");
    }

    public void testMap() {
        Bean4 b4 = (Bean4) ctxt.getBean("bean4");
        assertEquals(b4.getName(), "Steve");
        assert b4.getMap() != null;
        assert b4.getMap().size() == 3;
        assertEquals(b4.getMap().get("foo"), "baz");
        assert b4.getMap().get("bean1") instanceof Bean1;
        assert b4.getMap().get("bean2") instanceof Bean2 : b4.getMap().get("bean2");
    }

    public void testInnerBean() {
        Bean4 b4 = (Bean4) ctxt.getBean("bean4");
        assert b4.getAnotherBean() instanceof Bean3;
        Bean3 another = (Bean3) b4.getAnotherBean();
        assertEquals(another.getName(), "Pete");
        assert another.getBean2() != null;
    }

    public void testAlias() {
        Bean4 b4 = (Bean4) ctxt.getBean("bean4");
        Bean4 b4_alias = (Bean4) ctxt.getBean("bean4-alias");

        assert b4 != null;
        assert b4 == b4_alias;
    }

    public void testCreateAndDestroy() throws Exception {
        ConfigurableApplicationContext context = createContext();
        Bean1 b1 = (Bean1) context.getBean("bean1");
        assert b1.wasInitialised();
        context.close();
        assert b1.wasDestroyed();
    }


}
