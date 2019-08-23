package com.webflux.jsonp;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * @Author huaili
 * @Date 2019/8/22 20:28
 * @Description JsonPServerWebExchangeDecorator
 **/
public class JsonPServerWebExchangeDecorator extends ServerWebExchangeDecorator {


    private JsonPServerHttpResponseDecorator responseDecorator;

    public JsonPServerWebExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        responseDecorator = new JsonPServerHttpResponseDecorator(delegate.getResponse(),delegate.getRequest());
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}
