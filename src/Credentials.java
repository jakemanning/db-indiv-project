import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * User should have credentials.txt file with
 * username and password, in the following format:
 * key = value
 *
 * https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html#load-java.io.Reader-
 */
public class Credentials {

    public final String username;
    public final String password;

    public Credentials() {
        Properties properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream("credentials.txt");
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            System.err.println("Please include a config file");
            e.printStackTrace();
        }
        username = properties.getProperty("username");
        password = properties.getProperty("password");
    }
}
