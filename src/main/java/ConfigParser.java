import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

public class ConfigParser {
    private static final String PATH = "src/main/resources/settings.yaml";
    private final String host;
    private final Integer port;

    public ConfigParser() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(PATH);

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);

        host = data.get("host").toString();
        port = (Integer) data.get("port");
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
