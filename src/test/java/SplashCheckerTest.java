import com.isneaker.bot.SplashChecker;
import org.asynchttpclient.Response;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Cameron on 5/18/2017.
 */
public class SplashCheckerTest {

    @Test
    public void checkConnection() {
        SplashChecker task = new SplashChecker("http://www.adidas.com/", null);

        Response r  = task.test();

        assertEquals(200, r.getStatusCode());
    }

}
