import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class StaticServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null); // creates a default executor
        System.out.println("Static File Server running on http://localhost:3000");
        server.start();
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            File file = new File(".." + path);
            System.out.println("Requested file: " + file.getAbsolutePath());
            if (!file.exists()) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            String mime = "text/html";
            if (path.endsWith(".css"))
                mime = "text/css";
            else if (path.endsWith(".js"))
                mime = "application/javascript";
            else if (path.endsWith(".png"))
                mime = "image/png";

            exchange.getResponseHeaders().add("Content-Type", mime);
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody(); FileInputStream fs = new FileInputStream(file)) {
                fs.transferTo(os);
            }
        }
    }
}
