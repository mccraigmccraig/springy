package springy;

import springy.context.SpringyApplicationContext;
import springy.context.RuntimeSpringyContext;
import org.jruby.Ruby;
import org.springframework.core.io.ClassPathResource;

public class RuntimeSpringyContextSerializationTests extends AbstractContextSerializationTests {
    protected SpringyApplicationContext doCreateContext() {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(), getContextResource(), false);
    }
}
