import java.net.http.HttpClient;
import java.net.URI;
import model.AuthData;
import model.GameData;
import requests.*;
import results.*;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;



public class ServerFacade {
    private final String serverString;
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();

    private <T> T makeRequest(String method, String path, Object body, String authToken, Class<T> responseClass) throws Exception {
        var url = URI.create(serverString + path);
        var requestBuilder = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json");

        if (authToken != null) {
            requestBuilder.header("Authorization", authToken);
        }

        if(body != null){
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(gson.toJson(body)));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        var response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

        if(response.statusCode() >= 400){
            throw new Exception(
                "Error: " + response.body()
            );
        }

        if(responseClass != null && !response.body().isEmpty()) {
            return gson.fromJson(response.body(), responseClass);
        }
        return null;
    }

    public ServerFacade(String serverString) {
        this.serverString = serverString;
    }

    public ServerFacade(int port) {
        this.serverString = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var body = new RegisterRequest(username, password, email);
        return makeRequest("POST", path, body, null, AuthData.class);
    };

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var body = new LoginRequest(username, password);
        return makeRequest("POST", path, body, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        var body = new LogoutRequest(authToken);
        makeRequest("DELETE", path, body,  authToken, null);
    }

}
