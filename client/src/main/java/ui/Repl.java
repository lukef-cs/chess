package ui;

import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import results.ListGamesResult;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;
import client.ServerFacade;
import client.websocket.ServerMessageObserver;
import client.websocket.WebSocketFacade;

public class Repl implements ServerMessageObserver{
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken = null;
    private WebSocketFacade ws;

    private State state = State.PRELOGIN;
    private List<GameData> currentGameList = new ArrayList<>();

    // Gameplay state
    private GameData activeGame;
    private String playerColor;

    enum State {PRELOGIN, POSTLOGIN, GAMEPLAY};

    public Repl(String serverUrl) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
    }

    private void getUpdatedGameList(String authToken) throws Exception{
        var listResult = server.listGames(authToken);
        currentGameList = listResult.games();
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadMsg = (LoadGameMessage) message;
                this.activeGame = loadMsg.getGame();
                System.out.println("\n" + drawBoard(activeGame.game(), playerColor, null));
                printPrompt();
            }
            case ERROR -> {
                ErrorMessage errorMsg = (ErrorMessage) message;
                System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + errorMsg.getErrorMessage());
                printPrompt();
            }
            case NOTIFICATION -> {
                NotificationMessage notifMsg = (NotificationMessage) message;
                System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_BLUE + notifMsg.getMessage());
                printPrompt();
            }
        }
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

        return switch (state) {
            case PRELOGIN -> evalPrelogin(cmd, tokens);
            case POSTLOGIN -> evalPostlogin(cmd, tokens);
            case GAMEPLAY -> evalGameplay(cmd, tokens);
        };
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
                if (tokens.length != 4){
                    yield "Expected: register <USERNAME> <PASSWORD> <EMAIL>";
                }
                var auth = server.register(tokens[1], tokens[2], tokens[3]);
                authToken = auth.authToken();
                state = State.POSTLOGIN;
                yield "Logged in as " + tokens[1];
            }
            case "login" -> {
                if (tokens.length != 3) {
                    yield "Expected: login <USERNAME> <PASSWORD>";
                }
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
                if (tokens.length != 2) {
                    yield "Expected: create <NAME>";
                }
                var result = server.createGame(tokens[1], authToken);
                var listResult = server.listGames(authToken);
                currentGameList = listResult.games();

                int position = 0;
                for (int i = 0; i < currentGameList.size(); i++) {
                    if (currentGameList.get(i).gameID() == result.gameID()) {
                        position = i + 1;
                        break;
                    }
                }
                yield "Created game '" + tokens[1] + "' (use 'list' to see all games, or 'join " + position + " [WHITE|BLACK]')";
            }
            case "list" -> {
                var result = server.listGames(authToken);
                currentGameList = result.games();
                yield formatGameList();
            }
            case "join" -> {
                if (tokens.length != 3 ) {
                    yield "Expected: join <ID> [WHITE|BLACK]";
                }
                if (!tokens[1].matches("\\d+")) {
                    yield "Game ID must be a number. Use 'list' to see games.";
                }
                getUpdatedGameList(authToken);
                int gameNum = Integer.parseInt(tokens[1]);
                if (gameNum < 1 || gameNum > currentGameList.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }

                String color = tokens[2].toUpperCase();
                if(!(color.equals("WHITE") || color.equals("BLACK"))){
                    yield "Must join game as white or black";
                }
                var game = currentGameList.get(gameNum - 1);
                server.joinGame(game.gameID(), tokens[2].toUpperCase(), authToken);

                ws = new WebSocketFacade(serverUrl, this);
                ws.joinGame(authToken, game.gameID());

                this.activeGame = game;
                this.playerColor = tokens[2].toUpperCase();
                this.state = State.GAMEPLAY;

                yield "Joined game as " + tokens[2] + "\n" + drawBoard(game.game(), tokens[2], null);
            }
            case "observe" -> {
                if (tokens.length != 2){
                     yield "Expected: observe <ID>";
                }
                // check if number
                if (!tokens[1].matches("\\d+")) {
                    yield "Game ID must be a number. Use 'list' to see games.";
                }
                getUpdatedGameList(authToken);
                int gameNum = Integer.parseInt(tokens[1]);
                if (gameNum < 1 || gameNum > currentGameList.size()) {
                    yield "Invalid game number. Use 'list' to see games.";
                }
                var game = currentGameList.get(gameNum - 1);

                ws = new WebSocketFacade(serverUrl, this);
                ws.joinGame(authToken, game.gameID());

                this.activeGame = game;
                this.playerColor = null;
                this.state = State.GAMEPLAY;

                yield "Observing game\n" + drawBoard(game.game(), "WHITE", null);
            }
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private String evalGameplay(String cmd, String[] tokens) throws Exception {
        return switch (cmd) {
            case "help" -> """
                    redraw - the board
                    leave - the game
                    move <from> <to> [promotion] - make a move (e.g. e2 e4, or a7 a8 q)
                    resign - forfeit the game
                    highlight <pos> - show legal moves (e.g. e2)
                    help - with possible commands
                    """;
            case "redraw" -> drawBoard(activeGame.game(), playerColor, null);
            case "leave" -> {
                ws.leave(authToken, activeGame.gameID());
                state = State.POSTLOGIN;
                ws = null;
                activeGame = null;
                yield "Left the game.";
            }
            case "move" -> {
                if (tokens.length < 3) yield "Expected: move <from> <to> [promotion]";
                String promotion = tokens.length > 3 ? tokens[3].toUpperCase() : null;
                ChessPiece.PieceType promoType = null;
                if (promotion != null) {
                    promoType = switch(promotion) {
                        case "Q" -> ChessPiece.PieceType.QUEEN;
                        case "R" -> ChessPiece.PieceType.ROOK;
                        case "B" -> ChessPiece.PieceType.BISHOP;
                        case "N" -> ChessPiece.PieceType.KNIGHT;
                        default -> null;
                    };
                }

                ChessMove move = new ChessMove(parsePosition(tokens[1]), parsePosition(tokens[2]), promoType);
                ws.makeMove(authToken, activeGame.gameID(), move);
                yield "Making move...";
            }
            case "resign" -> {
                System.out.print("Are you sure you want to resign? (yes/no): ");
                Scanner s = new Scanner(System.in);
                String answer = s.nextLine().trim().toLowerCase();
                if (answer.equals("yes")) {
                    ws.resign(authToken, activeGame.gameID());
                    yield "Resigning...";
                } else {
                    yield "Resignation cancelled.";
                }
            }
            case "highlight" -> {
                if (tokens.length != 2) yield "Expected: highlight <pos>";
                try {
                    ChessPosition pos = parsePosition(tokens[1]);
                    var moves = activeGame.game().validMoves(pos);
                    if (moves == null || moves.isEmpty()) {
                        yield "No legal moves for that piece.";
                    }
                    Set<ChessPosition> targets = new HashSet<>();
                    for(var m : moves) targets.add(m.getEndPosition());
                    yield drawBoard(activeGame.game(), playerColor, targets);
                } catch (Exception e) {
                    yield "Invalid position.";
                }
            }
            default -> "Unknown command. Type 'help' for options.";
        };
    }

    private ChessPosition parsePosition(String pos) throws Exception {
        if (pos.length() != 2) throw new Exception("Invalid position format");
        char colChar = pos.charAt(0);
        char rowChar = pos.charAt(1);
        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;
        if (col < 1 || col > 8 || row < 1 || row > 8) throw new Exception("Position out of bounds");
        return new ChessPosition(row, col);
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

        private String drawBoard(ChessGame game, String perspective, Set<ChessPosition> highlights) {
        var board = game.getBoard();
        boolean isWhitePerspective = perspective == null || perspective.equalsIgnoreCase("WHITE");
        StringBuilder sb = new StringBuilder();

        // Top Border
        sb.append("    ");
        for (int col = 0; col < 8; col++) {
            char letter = (char) ('a' + (isWhitePerspective ? col : 7 - col));
            sb.append(" ").append(letter).append(" ");
        }
        sb.append("\n");

        // Board Rows
        for (int row = 0; row < 8; row++) {
            int displayRow = isWhitePerspective ? (8 - row) : (row + 1);
            sb.append(" ").append(displayRow).append(" "); // Left Row Number

            for (int col = 0; col < 8; col++) {
                int r = isWhitePerspective ? (7 - row) : row;
                int c = isWhitePerspective ? col : (7 - col);
                ChessPosition position = new ChessPosition(r + 1, c + 1);
                ChessPiece piece = board.getPiece(position);

                boolean isDarkSquare = (r + c) % 2 == 0;

                // Determine background color
                String bgColor;
                if (highlights != null && highlights.contains(position)) {
                    // Highlighted square (Green)
                    bgColor = isDarkSquare ? EscapeSequences.SET_BG_COLOR_DARK_GREEN : EscapeSequences.SET_BG_COLOR_GREEN;
                } else {
                    // Normal square
                    bgColor = isDarkSquare ? EscapeSequences.SET_BG_COLOR_BLACK : EscapeSequences.SET_BG_COLOR_WHITE;
                }

                sb.append(bgColor);

                if (piece != null){
                    sb.append(getPieceSymbol(piece));
                } else {
                    sb.append(EscapeSequences.EMPTY);
                }

                sb.append(EscapeSequences.RESET_BG_COLOR);
            }

            sb.append(" ").append(displayRow).append("\n"); // Right Row Number
        }

        // Bottom Border
        sb.append("    ");
        for (int col = 0; col < 8; col++) {
            char letter = (char) ('a' + (isWhitePerspective ? col : 7 - col));
            sb.append(" ").append(letter).append(" ");
        }
        sb.append("\n");

        return sb.toString();
    }

    private String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == chess.ChessGame.TeamColor.WHITE;
        String symbol;
        switch (piece.getPieceType()) {
            case KING:
                symbol = isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
                break;
            case QUEEN:
                symbol = isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
                break;
            case ROOK:
                symbol = isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
                break;
            case BISHOP:
                symbol = isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
                break;
            case KNIGHT:
                symbol = isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
                break;
            case PAWN:
                symbol = isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
                break;
            default:
                symbol = "?";
        }
        return symbol;
    }
}
