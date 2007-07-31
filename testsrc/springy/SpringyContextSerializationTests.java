package springy;

import springy.context.SpringyApplicationContext;
import springy.context.SpringyContext;
import org.springframework.core.io.ClassPathResource;

/**
 * TODO: comment
 */
public class SpringyContextSerializationTests extends AbstractContextSerializationTests{
    protected SpringyApplicationContext doCreateContext() {
        return new SpringyContext( getContextResource(), false);
    }
}
