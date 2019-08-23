package com.webflux.jsonp;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @Author huaili
 * @Date 2019/8/22 19:41
 * @Description JsonpConfigWebFluxFilter
 **/
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE) //过滤器顺序
public class JsonpConfigWebFluxFilter implements WebFilter{

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        return webFilterChain.filter(new JsonPServerWebExchangeDecorator(serverWebExchange));
    }


}
