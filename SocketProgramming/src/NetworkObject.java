import java.util.ArrayList;
import java.util.List;

public class NetworkObject extends Object{
    enum Method {
        GET, POST, PUT, DELETE
    }

    List<Query> query;
    int statusCode;
    String responseBody;
    Method method;
    String endpoint;

    public NetworkObject() {
    }

    public NetworkObject(String request) {
        // Parse request

    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void addQuery(Query query) {
        this.query.add(query);
    }

    public List<Query> getQuery() {
        return query;
    }

    public void removaAllQuery() {
        query.clear();
    }

    public String toString() {
        return "NetworkObject";
    }
}
