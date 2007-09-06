package springy;

import springy.context.SpringyApplicationContext;
import springy.context.BSFSpringyContext;

public class SpringyContextSerializationTests extends AbstractContextSerializationTests{
    protected SpringyApplicationContext doCreateContext() {
        return new BSFSpringyContext( false, getContextResource());
    }
}
