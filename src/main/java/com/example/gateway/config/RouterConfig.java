package com.example.gateway.config;

import com.example.gateway.handler.UserInfoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * WebFluxのルーティング設定
 * 
 * Spring Cloud GatewayのバックエンドAPIエンドポイントを
 * RouterFunctionで定義します。Spring MVCの代わりに
 * WebFluxベースのルーティングを使用します。
 * 
 * @author Claude
 * @version 1.0.0
 */
@Configuration
public class RouterConfig {

    /**
     * バックエンドAPIのルーティング設定
     * 
     * /api/** パスのエンドポイントをRouterFunctionで定義します。
     * これらのエンドポイントは、Gatewayからプロキシされたリクエストを
     * 処理するために使用されます。
     * 
     * @param userInfoHandler ユーザー情報ハンドラー
     * @return RouterFunction
     */
    @Bean
    public RouterFunction<ServerResponse> apiRoutes(UserInfoHandler userInfoHandler) {
        return RouterFunctions
                .route(GET("/api/user-info")
                        .and(accept(MediaType.APPLICATION_JSON)), 
                        userInfoHandler::getUserInfo)
                .andRoute(GET("/api/health")
                        .and(accept(MediaType.APPLICATION_JSON)), 
                        userInfoHandler::health);
    }

    /**
     * 追加のルーティング設定（必要に応じて拡張）
     * 
     * 将来的に新しいエンドポイントを追加する場合は、
     * ここに新しいRouterFunctionを定義できます。
     * 
     * @return RouterFunction
     */
    @Bean
    public RouterFunction<ServerResponse> additionalRoutes() {
        return RouterFunctions
                .route(GET("/api/info"), 
                        request -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(java.util.Map.of(
                                        "application", "Spring Cloud Gateway Demo",
                                        "version", "1.0.0",
                                        "description", "WebFlux-based backend API",
                                        "timestamp", java.time.Instant.now().toString()
                                )));
    }
}