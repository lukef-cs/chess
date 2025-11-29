package websocket.messages;

public class ErrorMessage extends ServerMessage{
    private String error;

    public ErrorMessage(String error){
        super(ServerMessageType.ERROR);
        this.error = error;
    }

    public String getErrorMessage() {
        return error;
    }
}
