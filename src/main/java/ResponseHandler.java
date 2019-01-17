import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.netty.request.NettyRequest;

import java.io.ByteArrayOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class ResponseHandler implements AsyncHandler<Response> {
    private final Context context;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final Response response = new Response();
    private final int index;


    public ResponseHandler(Context context, int index) {
        this.context = context;
        this.index = index;
    }


    @Override
    public void onRequestSend(NettyRequest request) {
        response.setStart(System.nanoTime());
    }


    @Override
    public State onStatusReceived(HttpResponseStatus httpResponseStatus) throws Exception {
        response.setHttpStatusCode(httpResponseStatus.getStatusCode());
        return State.CONTINUE;
    }


    @Override
    public State onHeadersReceived(HttpHeaders httpHeaders) throws Exception {
        response.setHeaders(httpHeaders);
        return State.CONTINUE;
    }


    @Override
    public State onBodyPartReceived(HttpResponseBodyPart httpResponseBodyPart) throws Exception {
        baos.write(httpResponseBodyPart.getBodyPartBytes());
        return State.CONTINUE;
    }


    @Override
    public void onThrowable(Throwable throwable) {
        response.setError(true);
        //context.onError(response);
        System.out.println(throwable.getClass().getCanonicalName());
        ConnectException exception = (ConnectException) throwable;
        exception.getMessage();
    }


    @Override
    public void onTcpConnectFailure(InetSocketAddress remoteAddress, Throwable cause) {
        response.setEnd(System.nanoTime());
    }


    @Override
    public void onConnectionPoolAttempt() {
        response.setStart(System.nanoTime());
    }


    @Override
    public void onConnectionPooled(Channel connection) {
        response.setStart(System.nanoTime());
    }


    @Override
    public Response onCompleted() throws Exception {
        response.setEnd(System.nanoTime());
        response.setBody(baos.toByteArray());
        return response;
    }

}
