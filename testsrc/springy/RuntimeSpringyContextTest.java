package springy;

import org.jruby.Ruby;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;
import springy.context.RuntimeSpringyContext;

@Test
public class RuntimeSpringyContextTest extends SpringyContextTests {

    protected ConfigurableApplicationContext createContext() throws Exception {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(),
                new ClassPathResource("/springy/context.rb"));
    }
}
