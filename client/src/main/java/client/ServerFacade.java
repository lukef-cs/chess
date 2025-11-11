package client;

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

        System.out.println("DEBUG: HTTP Status: " + response.statusCode());  // Debug
        System.out.println("DEBUG: Response Body: " + response.body());     // Debug

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
        System.out.println("DEBUG: Calling register for " + username);  // Debug
        var path = "/user";
        var body = new RegisterRequest(username, password, email);
        var result = makeRequest("POST", path, body, null, AuthData.class);
        System.out.println("DEBUG: Register result: " + result);  // Debug
        if (result != null) {
            System.out.println("DEBUG: Auth token: " + result.authToken());  // Debug
        }
        return result;
    }

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

    public CreateGameResult createGame(String gameName, String authToken) throws Exception {
        var path = "/game";
        var body = new CreateGameRequest(gameName);
        return makeRequest("POST", path, body, authToken, CreateGameResult.class);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        var path = "/game";
        return makeRequest("GET", path, null, authToken, ListGamesResult.class);
    }

    public void joinGame(int gameID, String playerColor, String authToken) throws Exception {
        var path = "/game";
        var body = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", path, body, authToken, null);
    }

    public void clear() throws Exception {
        var path = "/db";
        makeRequest("DELETE", path, null, null, null);
    }
}
