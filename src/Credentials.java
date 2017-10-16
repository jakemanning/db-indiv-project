import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Credentials {

    public final String username;
    public final String password;

    public Credentials() throws IOException {
        final Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream("credentials.txt")) {
            properties.load(inputStream);
            username = properties.getProperty("username");
            password = properties.getProperty("password");
        }
    }
}