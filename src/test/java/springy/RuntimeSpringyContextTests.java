package springy;

import org.jruby.Ruby;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;
import org.testng.Assert;
import springy.context.RuntimeSpringyContext;
import springy.beans.Bean6;

@Test
public class RuntimeSpringyContextTests extends SpringyContextTests {

    protected ConfigurableApplicationContext createContext() throws Exception {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(),
                new ClassPathResource("springy/context.rb"));
    }

}
