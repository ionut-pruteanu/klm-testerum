package klm.model.context;

public class Context {
    public String accessToken;
    public String baseUrl;

    @Override
    public String toString() {
        return "Context{" +
            "accessToken='" + accessToken + '\'' +
            ", baseUrl='" + baseUrl + '\'' +
            '}';
    }
}
