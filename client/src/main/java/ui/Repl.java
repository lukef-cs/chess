package ui;

import java.util.*;
import model.GameData;
import results.ListGamesResult;
import client.ServerFacade;

public class Repl {
    private final ServerFacade server;
    private String authToken = null;
    private State state = State.PRELOGIN;
    private List<GameData> currentGameList = new ArrayList<>();

    enum State {PRELOGIN, POSTLOGIN};

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_WHITE +
                          "♕ Welcome to 240 Chess. Type Help to get started. ♕");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printPrompt();
            String line = scanner.nextLine().trim();

            try {
                if (line.equalsIgnoreCase("quit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                String result = eval(line);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + result);
            } catch (Exception e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + e.getMessage());
            }
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    private void printPrompt() {
        System.out.print("\n" + EscapeSequences.RESET_TEXT_COLOR +
                        (state == State.PRELOGIN ? "[LOGGED_OUT] >>> " : "[LOGGED_IN] >>> "));
    }

    private String eval(String input) throws Exception {
        var tokens = input.split("\\s+");
        var cmd = tokens.length > 0 ? tokens[0].toLowerCase() : "help";

        return (state == State.PRELOGIN) ? evalPrelogin(cmd, tokens) : evalPostlogin(cmd, tokens);
    }

    private String evalPrelogin(String cmd, String[] tokens) throws Exception {
        return switch (cmd) {
            case "help" -> """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
            case "register" -> {
                if (tokens.length < 4) yield "Expected: register <USERNAME> <PASSWORD> <EMAIL>";
                var auth = server.register(tokens[1], tokens[2], tokens[3]);
                authToken = auth.authToken();
                state = State.POSTLOGIN;
                yield "Logged in as " + tokens[1];
            }
            case "login" -> {
                if (tokens.length < 3) yield "Expected: login <USERNAME> <PASSWORD>";
                var auth = server.login(tokens[1], tokens[2]);
                authToken = auth.authToken();
                state = State.POSTLOGIN;
                yield "Logged in as " + tokens[1];
            }
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String evalPostlogin(String cmd, String[] tokens) throws Exception {
        return switch (cmd) {
            case "help" -> """
                    create <NAME> - a game
                    list - games
                    join <ID> [WHITE|BLACK] - a game
                    observe <ID> - a game
                    logout - when you are done
                    quit - playing chess
                    help - with possible commands
                    """;
            case "logout" -> {
                server.logout(authToken);
                authToken = null;
                state = State.PRELOGIN;
                yield "Logged out successfully";
            }
            case "create" -> {
                if (tokens.length < 2) yield "Expected: create <NAME>";
                var result = server.createGame(tokens[1], authToken);
                yield "Created game with ID: " + result.gameID();
            }
            case "list" -> {
                var result = server.listGames(authToken);
                currentGameList = result.games();
                yield formatGameList();
            }
            case "join" -> {
                if (tokens.length < 3) yield "Expected: join <ID> [WHITE|BLACK]";
                int gameNum = Integer.parseInt(tokens[1]);
                if (gameNum < 1 || gameNum > currentGameList.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }
                var game = currentGameList.get(gameNum - 1);
                server.joinGame(game.gameID(), tokens[2].toUpperCase(), authToken);
                yield "Joined game as " + tokens[2] + "\n" + drawBoard(game, tokens[2]);
            }
            case "observe" -> {
                if (tokens.length < 2) yield "Expected: observe <ID>";
                int gameNum = Integer.parseInt(tokens[1]);
                if (gameNum < 1 || gameNum > currentGameList.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }
                var game = currentGameList.get(gameNum - 1);
                yield "Observing game\n" + drawBoard(game, "WHITE");
            }
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String formatGameList() {
        if (currentGameList.isEmpty()) {
            return "No games available.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentGameList.size(); i++) {
            var game = currentGameList.get(i);

            sb.append(String.format("%d. Name: %s | White: %s | Black: %s\n",
                    i + 1,
                    game.gameName(),
                    game.whiteUsername() != null ? game.whiteUsername() : "available",
                    game.blackUsername() != null ? game.blackUsername() : "available"));
        }
        return sb.toString();
    }

    private String drawBoard(GameData game, String perspective) {
        return "Board drawing coming soon...";
    }
}
