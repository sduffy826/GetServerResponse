import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Little helper just to return a properties file for the string (properties) filename passed in
 * @author sduffy
 *
 */
public class PropertyHelper {
  public static Properties getProperties(String propsFile) {
    Properties theProps = new Properties();
    try {
      theProps.load(new FileInputStream(propsFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      theProps = null;
    } catch (IOException e) {
      e.printStackTrace();
      theProps = null;
    };
    return theProps;
  }
}
