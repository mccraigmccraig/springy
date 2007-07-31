package springy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.AfterClass;
import springy.beans.MyInterface;
import springy.beans.Bean1;

/**
 * XmlApplicationContext specific tests.
 */
@Test
public class XmlApplicationContextTests extends AbstractApplicationContextTests {

    @BeforeClass
    public void beforeClass() throws Exception {
        //long start = System.currentTimeMillis();
        ctxt = createContext();
        //System.err.println("context created in " + (System.currentTimeMillis() - start) + " ms");
    }

    @AfterClass
    public void afterClass() throws Exception
    {
        ctxt.close();
    }

    protected ConfigurableApplicationContext createContext() {
        return new ClassPathXmlApplicationContext(new String[]{"/springy/context.xml"}, true);
    }
}
