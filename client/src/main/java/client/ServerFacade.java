package client;

import java.net.http.HttpClient;
import java.io.IOException;
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

        try {

            var response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                // Parse the error message from JSON: {"message": "Error: bad request"}
                String userFriendlyMessage = "An unknown error occurred.";
                try {
                    var errorMap = gson.fromJson(response.body(), java.util.Map.class);
                    String rawMessage = (String) errorMap.get("message");
                    if (rawMessage != null) {
                        // Map server error messages to user-friendly ones
                        if (rawMessage.contains("bad request")) {
                            userFriendlyMessage = "Invalid input. Please check your command and try again.";
                        } else if (rawMessage.contains("unauthorized")) {
                            userFriendlyMessage = "You are not logged in or your session has expired. Please log in again.";
                        } else if (rawMessage.contains("color already taken")) {
                            userFriendlyMessage = "That color is already taken in this game.";
                        } else if (rawMessage.contains("already exists")) {
                            userFriendlyMessage = "A user with that username already exists.";
                        } else if (rawMessage.contains("invalid credentials")) {
                            userFriendlyMessage = "Invalid username or password. Please try again.";
                        } else {
                            // Use the raw message if it starts with "Error:" (strip it for readability)
                            userFriendlyMessage = rawMessage.startsWith("Error: ") ? rawMessage.substring(7) : rawMessage;
                        }
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, use a generic message
                    userFriendlyMessage = "Server error. Please try again later.";
                }
                throw new Exception(userFriendlyMessage);
            }

            if(responseClass != null && !response.body().isEmpty()) {
                return gson.fromJson(response.body(), responseClass);
            }
            return null;
        } catch (IOException e) {
            // Handle connection failures (server down, network issues)
            throw new Exception("Unable to connect to the server. Please check if the server is running and try again.");
        } catch (InterruptedException e) {
            // Handle interruptions (e.g., thread interrupted)
            Thread.currentThread().interrupt();
            throw new Exception("Request was interrupted. Please try again.");
        }
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
        var result = makeRequest("POST", path, body, null, AuthData.class);

        return result;
    }

    // if theres too many params, print the "expected" string
    // bottom right square should always be white
    // shouldn't show json objects for error messages

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
