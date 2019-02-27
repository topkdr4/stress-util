package ru.vetoshkin.stress;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.netty.request.NettyRequest;
import ru.vetoshkin.stress.context.Context;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;





/**
 * Ветошкин А.В. РИС-16бзу
 * */
public class ResponseHandler implements AsyncHandler<Response> {
    private final Context context;
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final Response response = new Response();


    public ResponseHandler(ru.vetoshkin.stress.context.Context context) {
        this.context = context;
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
        response.setResponseHeaders(httpHeaders);
        return State.CONTINUE;
    }


    @Override
    public State onBodyPartReceived(HttpResponseBodyPart httpResponseBodyPart) throws Exception {
        baos.write(httpResponseBodyPart.getBodyPartBytes());
        return State.CONTINUE;
    }


    @Override
    public void onThrowable(Throwable throwable) {
        response.setEnd(System.nanoTime());
        response.setError(true);
        context.onError(response);
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
