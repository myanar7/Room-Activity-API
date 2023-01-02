
public class Query {
    String key;
    String value;

    public Query(String[] pair) {
        if (pair.length != 2) {
            throw new IllegalArgumentException("Query must be in the form of key=value");
        }
        key = pair[0];
        value = pair[1];
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "Query";
    }


}
