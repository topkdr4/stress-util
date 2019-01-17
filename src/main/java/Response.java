import io.netty.handler.codec.http.HttpHeaders;
import lombok.Getter;
import lombok.Setter;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
@Getter
@Setter
public class Response {
    private byte[] body;
    private boolean error;
    private long start;
    private long end;

    private int httpStatusCode;
    private HttpHeaders headers;


    @Override
    public String toString() {
        return "DIFF: " + (end - start) + " IS_ERROR: " + error;
    }
}
