import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import es.uji.apps.par.config.Configuration;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class BaseResourceTest extends JerseyTest
{
	@Autowired
	Configuration configuration;

    public BaseResourceTest(WebAppDescriptor build)
    {
        super(build);

        desactivarAppenderLogGmail();
    }

    private void desactivarAppenderLogGmail()
    {
        configuration.desactivaLogGmail();
    }
}