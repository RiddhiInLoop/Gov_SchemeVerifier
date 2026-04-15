// backend/Main.java
package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws Exception {
        // Initialize the DB Connection once when server starts
        DBConnection.getConnection();

        // Create an HTTP Server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Define our basic routing
        server.createContext("/api/schemes", new SchemesHandler());
        server.createContext("/api/ping", new PingHandler());
        server.createContext("/api/citizens/register", new RegisterHandler());
        server.createContext("/api/citizens/login", new LoginHandler());

        server.createContext("/api/eligibility/check", new EligibilityCheckHandler());
        server.createContext("/api/applications/apply", new ApplySchemeHandler());
        server.createContext("/api/applications/citizen", new CitizenApplicationsHandler());

        server.createContext("/api/admin/login", new AdminLoginHandler());
        server.createContext("/api/admin/applications", new AdminApplicationsHandler());
        server.createContext("/api/admin/approve", new ApproveRejectHandler("approve"));
        server.createContext("/api/admin/reject", new ApproveRejectHandler("reject"));

        server.createContext("/api/admin/schemes", new AdminSchemeHandler());

        server.setExecutor(null); // creates a default executor

        System.out.println("Backend Server is running on http://localhost:8080");
        server.start();
    }

    // Helper method to extract a string value from our simple JSON payloads
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if (index == -1)
            return null;

        int startQuote = json.indexOf("\"", index + searchKey.length());
        if (startQuote == -1)
            return null;

        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1)
            return null;

        return json.substring(startQuote + 1, endQuote);
    }

    // Helper method to extract a numerical value from JSON
    private static String extractJsonNumber(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if (index == -1)
            return null;

        int start = index + searchKey.length();
        while (start < json.length()
                && (json.charAt(start) == ' ' || json.charAt(start) == ':' || json.charAt(start) == '"')) {
            start++;
        }

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
            end++;
        }

        if (start == end)
            return null;
        return json.substring(start, end);
    }

    // Handler for POST /api/citizens/register
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes());

                try {
                    String firstName = extractJsonValue(reqBody, "firstName");
                    String middleName = extractJsonValue(reqBody, "middleName");
                    String lastName = extractJsonValue(reqBody, "lastName");
                    int age = Integer.parseInt(extractJsonNumber(reqBody, "age"));
                    String gender = extractJsonValue(reqBody, "gender");
                    String category = extractJsonValue(reqBody, "category");
                    String citizenship = extractJsonValue(reqBody, "citizenship");
                    double income = Double.parseDouble(extractJsonNumber(reqBody, "income"));
                    String residenceType = extractJsonValue(reqBody, "residenceType");
                    String area = extractJsonValue(reqBody, "area");
                    String landmark = extractJsonValue(reqBody, "landmark");
                    String pinCode = extractJsonValue(reqBody, "pinCode");
                    String email = extractJsonValue(reqBody, "email");
                    String password = extractJsonValue(reqBody, "password");

                    CitizenService service = new CitizenService();
                    boolean success = service.registerCitizen(firstName, middleName, lastName, age, gender, category,
                            citizenship, income, residenceType, area, landmark, pinCode,
                            email, password);

                    String response = "{\"success\":" + success + "}";
                    int code = success ? 200 : 400;

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(code, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.err.println("Error parsing register JSON: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for POST /api/citizens/login
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes());

                try {
                    String email = extractJsonValue(reqBody, "email");
                    String password = extractJsonValue(reqBody, "password");

                    CitizenService service = new CitizenService();
                    int citizenId = service.loginCitizen(email, password);

                    if (citizenId != -1) {
                        String citizenJson = service.getCitizenById(citizenId);
                        // Inject role dynamically
                        String finalJson = citizenJson.replace("}", ", \"role\":\"citizen\"}");

                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, finalJson.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(finalJson.getBytes());
                        os.close();
                    } else {
                        String response = "{\"error\":\"Invalid credentials\"}";
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(401, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing login JSON: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for GET /api/schemes
    static class SchemesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Enable CORS so our frontend HTML can communicate with this backend
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                SchemeService service = new SchemeService();

                // Fetch from DB and build a basic JSON Array string manually for the student
                // project
                StringBuilder jsonResponse = new StringBuilder("[");
                var schemes = service.getAllSchemes();
                for (int i = 0; i < schemes.size(); i++) {
                    jsonResponse.append(schemes.get(i).toJson());
                    if (i < schemes.size() - 1)
                        jsonResponse.append(",");
                }
                jsonResponse.append("]");

                String response = jsonResponse.toString();
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }

    // Handler for GET /api/eligibility/check
    static class EligibilityCheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    int citizenId = -1;
                    int schemeId = -1;

                    if (query != null) {
                        String[] pairs = query.split("&");
                        for (String pair : pairs) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                if (kv[0].equals("citizenId"))
                                    citizenId = Integer.parseInt(kv[1]);
                                if (kv[0].equals("schemeId"))
                                    schemeId = Integer.parseInt(kv[1]);
                            }
                        }
                    }

                    if (citizenId != -1 && schemeId != -1) {
                        EligibilityService service = new EligibilityService();
                        String eligibilityResult = service.checkEligibility(citizenId, schemeId);
                        boolean isEligible = "Eligible".equals(eligibilityResult);

                        String response = "{\"eligible\":" + isEligible + ", \"message\":\"" + eligibilityResult
                                + "\"}";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                } catch (Exception e) {
                    System.err.println("Eligibility Check error: " + e.getMessage());
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Simple ping endpoint to verify backend reachability
    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "{\"ok\":true}";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for POST /api/applications/apply
    static class ApplySchemeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes());

                try {
                    int citizenId = Integer.parseInt(extractJsonNumber(reqBody, "citizenId"));
                    int schemeId = Integer.parseInt(extractJsonNumber(reqBody, "schemeId"));

                    ApplicationService service = new ApplicationService();
                    boolean applyResult = service.applyForScheme(citizenId, schemeId);
                    boolean success = applyResult;

                    String response = "{\"success\":" + success
                            + ", \"message\":\"Application submitted successfully.\"}";
                    int code = success ? 200 : 400;

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(code, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.err.println("Apply error: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for GET /api/applications/citizen/{id}
    static class CitizenApplicationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String[] segments = path.split("/");
                    if (segments.length > 0) {
                        int citizenId = Integer.parseInt(segments[segments.length - 1]);
                        ApplicationService service = new ApplicationService();
                        var apps = service.getCitizenApplications(citizenId);

                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < apps.size(); i++) {
                            ApplicationService.AppRecord app = apps.get(i);
                            // Simple manual JSON construction
                            sb.append(String.format(
                                    "{\"applicationId\":%d, \"schemeName\":\"%s\", \"appliedDate\":\"%s\", \"status\":\"%s\", \"rejectionReason\":%s}",
                                    app.applicationId,
                                    app.schemeName.replace("\"", "\\\""),
                                    app.appliedDate,
                                    app.status,
                                    app.rejectionReason == null ? "null"
                                            : "\"" + app.rejectionReason.replace("\"", "\\\"") + "\""));
                            if (i < apps.size() - 1)
                                sb.append(",");
                        }
                        sb.append("]");
                        String jsonResponse = sb.toString();

                        exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } else {
                        exchange.sendResponseHeaders(400, -1);
                    }
                } catch (Exception e) {
                    System.err.println("Fetch apps error: " + e.getMessage());
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for POST /api/admin/login
    static class AdminLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String reqBody = new String(exchange.getRequestBody().readAllBytes());
                try {
                    String username = extractJsonValue(reqBody, "username");
                    String password = extractJsonValue(reqBody, "password");

                    AdminService service = new AdminService();
                    boolean valid = service.adminLogin(username, password);

                    if (valid) {
                        String json = "{\"id\":999, \"name\":\"System Admin\", \"email\":\"" + username
                                + "\", \"role\":\"admin\"}";
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(200, json.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(json.getBytes());
                        os.close();
                    } else {
                        String response = "{\"error\":\"Invalid credentials\"}";
                        exchange.getResponseHeaders().add("Content-Type", "application/json");
                        exchange.sendResponseHeaders(401, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    }
                } catch (Exception e) {
                    System.err.println("Admin login error: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for GET /api/admin/applications
    static class AdminApplicationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    AdminService service = new AdminService();
                    var apps = service.viewAllApplications();

                    StringBuilder sb = new StringBuilder("[");
                    for (int i = 0; i < apps.size(); i++) {
                        sb.append(apps.get(i).toJson());
                        if (i < apps.size() - 1)
                            sb.append(",");
                    }
                    sb.append("]");
                    String jsonResponse = sb.toString();

                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(jsonResponse.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.err.println("Admin fetch apps error: " + e.getMessage());
                    exchange.sendResponseHeaders(500, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for POST /api/admin/approve/{id} and /api/admin/reject/{id}
    static class ApproveRejectHandler implements HttpHandler {
        private String action;

        public ApproveRejectHandler(String action) {
            this.action = action;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    String[] segments = path.split("/");
                    int applicationId = Integer.parseInt(segments[segments.length - 1]);

                    AdminService service = new AdminService();
                    boolean success = false;

                    if ("approve".equals(action)) {
                        success = service.updateApplicationStatus(applicationId, "Approved", null);
                    } else if ("reject".equals(action)) {
                        String reqBody = new String(exchange.getRequestBody().readAllBytes());
                        String reason = extractJsonValue(reqBody, "reason");
                        success = service.updateApplicationStatus(applicationId, "Rejected", reason);
                    }

                    String response = "{\"success\":" + success + "}";
                    int code = success ? 200 : 400;

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(code, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.err.println("Approve/Reject error: " + e.getMessage());
                    exchange.sendResponseHeaders(400, -1);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // Handler for Admin Scheme CRUD (POST = add, PUT = edit, DELETE = delete)
    static class AdminSchemeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                AdminService service = new AdminService();
                boolean success = false;

                if ("POST".equals(exchange.getRequestMethod())) {
                    String reqBody = new String(exchange.getRequestBody().readAllBytes());
                    String name = extractJsonValue(reqBody, "name");
                    String desc = extractJsonValue(reqBody, "description");
                    int minAge = Integer.parseInt(extractJsonNumber(reqBody, "minAge"));
                    int maxAge = Integer.parseInt(extractJsonNumber(reqBody, "maxAge"));
                    double incLimit = Double.parseDouble(extractJsonNumber(reqBody, "incomeLimit"));
                    String gender = extractJsonValue(reqBody, "genderReq");
                    String cat = extractJsonValue(reqBody, "categoryReq");
                    String cit = extractJsonValue(reqBody, "citizenshipReq");

                    success = service.addScheme(name, desc, minAge, maxAge, incLimit, gender, cat, cit);

                } else if ("PUT".equals(exchange.getRequestMethod())) {
                    String reqBody = new String(exchange.getRequestBody().readAllBytes());
                    int id = Integer.parseInt(extractJsonNumber(reqBody, "schemeId"));
                    String name = extractJsonValue(reqBody, "name");
                    String desc = extractJsonValue(reqBody, "description");
                    int minAge = Integer.parseInt(extractJsonNumber(reqBody, "minAge"));
                    int maxAge = Integer.parseInt(extractJsonNumber(reqBody, "maxAge"));
                    double incLimit = Double.parseDouble(extractJsonNumber(reqBody, "incomeLimit"));
                    String gender = extractJsonValue(reqBody, "genderReq");
                    String cat = extractJsonValue(reqBody, "categoryReq");
                    String cit = extractJsonValue(reqBody, "citizenshipReq");

                    success = service.updateScheme(id, name, desc, minAge, maxAge, incLimit, gender, cat, cit);

                } else if ("DELETE".equals(exchange.getRequestMethod())) {
                    String path = exchange.getRequestURI().getPath();
                    String[] segments = path.split("/");
                    int schemeId = Integer.parseInt(segments[segments.length - 1]);
                    success = service.deleteScheme(schemeId);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                String response = "{\"success\":" + success + "}";
                int code = success ? 200 : 400;

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(code, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } catch (Exception e) {
                System.err.println("Admin Scheme CRUD error: " + e.getMessage());
                exchange.sendResponseHeaders(400, -1);
            }
        }
    }
}
