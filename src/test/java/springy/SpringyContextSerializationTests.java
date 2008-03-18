package springy;

import springy.context.SpringyApplicationContext;
import springy.context.RuntimeSpringyContext;
import org.jruby.Ruby;
import org.testng.annotations.Test;

@Test
public class SpringyContextSerializationTests extends AbstractContextSerializationTests{
    protected SpringyApplicationContext doCreateContext() {
        return new RuntimeSpringyContext( Ruby.getDefaultInstance() , false, getContextResource());
    }
}
