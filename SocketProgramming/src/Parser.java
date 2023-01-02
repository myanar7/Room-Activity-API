public class Parser<T extends NetworkObject> {
    private Parser<T> parser = null;

    public Parser<T> getParser() {
        if (parser == null) {
            parser = new Parser<T>();
        }
        return parser;
    }

    public T getObject(String request) {
        if(T.class == NetworkObject.class) {
            return (T) new NetworkObject(request);
        } else 
            return null;
    }


}
