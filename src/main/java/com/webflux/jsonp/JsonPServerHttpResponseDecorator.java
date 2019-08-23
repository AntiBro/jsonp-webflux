package com.webflux.jsonp;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

import static reactor.core.scheduler.Schedulers.single;

/**
 * @Author huaili
 * @Date 2019/8/22 20:27
 * @Description JsonPServerHttpResponseDecorator
 **/
public class JsonPServerHttpResponseDecorator extends ServerHttpResponseDecorator {
    private static String JSONP_HEADR_VALUE = "text/javascript;charset=UTF-8";
    private static String JSONP_PREFIX = "(";
    private static String JSONP_SUFFIX = ");";
    private static String JSONP_CALLBACK = "callback";

    private static String CHARSET_UTF8 = "UTF-8";

    Logger log = LoggerFactory.getLogger(getClass());
    ServerHttpRequest serverHttpRequest;
    JsonPServerHttpResponseDecorator(ServerHttpResponse delegate,ServerHttpRequest serverHttpRequest) {
        super(delegate);
        this.serverHttpRequest = serverHttpRequest;
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return super.writeAndFlushWith(body);
    }
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        String callback = serverHttpRequest.getQueryParams().getFirst(JSONP_CALLBACK);
        if (body instanceof Mono) {
            final Mono<DataBuffer> monoBody = (Mono<DataBuffer>) body;
            return super.writeWith(monoBody.publishOn(single()).map(dataBuffer -> wrapJsonP(dataBuffer,callback)));
        }else if(body instanceof Flux){
            final Flux<DataBuffer> monoBody = (Flux<DataBuffer>) body;
            return super.writeWith(monoBody.publishOn(single()).map(dataBuffer -> wrapJsonP(dataBuffer,callback)));
        }

        return super.writeWith(body);
    }

    private  <T extends DataBuffer> T wrapJsonP(T buffer,String callback){
        try {
            HttpHeaders httpHeaders = super.getHeaders();
            InputStream dataBuffer = buffer.asInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            String oldContent = IOUtils.toString(dataBuffer,CHARSET_UTF8);

            if(StringUtils.isNotEmpty(callback)){
                stringBuilder.append(callback).append(JSONP_PREFIX).append(oldContent).append(JSONP_SUFFIX);
                httpHeaders.setContentType(MediaType.parseMediaType(JSONP_HEADR_VALUE));
            }else{
                stringBuilder.append(oldContent);
            }
            String newContent = stringBuilder.toString();
            // must reset the content length,otherwise client will not receive the http packet
            byte[] newContentBytes = newContent.getBytes(CHARSET_UTF8);
            httpHeaders.setContentLength(newContentBytes.length);

            //NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
            if (log.isDebugEnabled()) {
                log.debug("return content: [{}]", new String(newContentBytes));
            }
            DataBufferUtils.release(buffer);
            return (T) super.bufferFactory().wrap(newContentBytes);
        } catch (IOException e) {
            DataBufferUtils.release(buffer);
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
