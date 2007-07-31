package springy;

import springy.context.SpringyApplicationContext;
import springy.context.SpringyContext;
import org.springframework.core.io.ClassPathResource;

public class SpringyContextSerializationTests extends AbstractContextSerializationTests{
    protected SpringyApplicationContext doCreateContext() {
        return new SpringyContext( getContextResource(), false);
    }
}
