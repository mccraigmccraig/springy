package springy;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.testng.annotations.Test;
import org.jruby.Ruby;
import springy.context.RuntimeSpringyContext;


@Test
public class ErrorHandlingTests {

    @Test(expectedExceptions = BeanDefinitionParsingException.class)
    public void testErrorHandling() {
        ApplicationContext ctxt = new RuntimeSpringyContext(Ruby.getDefaultInstance() , new ClassPathResource("springy/context-with-errors.rb"));
    }
}
