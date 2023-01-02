public class RequestObject extends NetworkObject{
    enum Method {
        GET, POST, PUT, DELETE
    }
    Method method;
    String path;
    String httpVersion;

    public RequestObject() {
        super();
    }

    public RequestObject(String request) {
        super(request);

        // Parse request
        String[] lines = request.split("\r");

        // Parse first line

        String[] firstLine = lines[0].split(" ");
        if (firstLine.length != 3) {
            throw new IllegalArgumentException("Request must be in the form of method path httpVersion");
        }
        method = Method.valueOf(firstLine[0]);
        path = firstLine[1];
        httpVersion = firstLine[2];

        // Parse query
        String[] pathSplit = path.split("\\?");
        if (pathSplit.length == 2) {
            path = pathSplit[0];
            String[] querySplit = pathSplit[1].split("&");
            for (String query : querySplit) {
                String[] pair = query.split("=");
                addQuery(new Query(pair));
            }
        }

        // Parse headers

        for (int i = 1; i < lines.length; i++) {
            String[] header = lines[i].split(": ");
            if (header.length != 2) {
                throw new IllegalArgumentException("Header must be in the form of key: value");
            }
            addHeader(new Header(header));
        }
        

    }
}
