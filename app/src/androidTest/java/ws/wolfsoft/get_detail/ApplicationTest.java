package ws.wolfsoft.get_detail;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test(){
        try {
            testApplicationTestCaseSetUpProperly ();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}