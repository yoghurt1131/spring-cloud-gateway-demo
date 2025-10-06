package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * リクエスト・レスポンスのログ出力を行うGatewayフィルターファクトリー
 * 
 * このフィルターは以下の機能を提供します：
 * 1. リクエスト受信時のログ出力（パス、メソッド、クエリパラメータ）
 * 2. レスポンス送信時のログ出力（ステータスコード、処理時間）
 * 
 * @author Claude
 * @version 1.0.0
 */
@Component
public class RequestLoggingGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<RequestLoggingGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingGatewayFilterFactory.class);

    public RequestLoggingGatewayFilterFactory() {
        super(Config.class);
    }

    /**
     * Gatewayフィルターを適用する
     * 
     * @param config フィルター設定
     * @return Gatewayフィルター
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // リクエスト開始時刻を記録
            long startTime = System.currentTimeMillis();
            
            // リクエスト情報をログ出力
            logRequestInfo(request, startTime);
            
            // レスポンス後の処理を設定
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                long endTime = System.currentTimeMillis();
                long processingTime = endTime - startTime;
                
                // レスポンス情報をログ出力
                logResponseInfo(request, response, processingTime);
            }));
        };
    }

    /**
     * リクエスト情報をログ出力する
     * 
     * @param request HTTPリクエスト
     * @param startTime 処理開始時刻
     */
    private void logRequestInfo(ServerHttpRequest request, long startTime) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String queryString = request.getURI().getQuery();
        String remoteAddress = getRemoteAddress(request);
        
        logger.info("=== リクエスト受信 ===");
        logger.info("メソッド: {}", method);
        logger.info("パス: {}", path);
        logger.info("クエリパラメータ: {}", queryString != null ? queryString : "なし");
        logger.info("リモートアドレス: {}", remoteAddress);
        logger.info("処理開始時刻: {}", java.time.Instant.ofEpochMilli(startTime));
        
        // デバッグレベルでヘッダー情報も出力
        if (logger.isDebugEnabled()) {
            logger.debug("リクエストヘッダー:");
            request.getHeaders().forEach((name, values) -> 
                logger.debug("  {}: {}", name, String.join(", ", values))
            );
        }
    }

    /**
     * レスポンス情報をログ出力する
     * 
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @param processingTime 処理時間（ミリ秒）
     */
    private void logResponseInfo(ServerHttpRequest request, ServerHttpResponse response, long processingTime) {
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        String statusText = response.getStatusCode() != null ? response.getStatusCode().toString() : "Unknown";
        
        logger.info("=== レスポンス送信 ===");
        logger.info("メソッド: {}", method);
        logger.info("パス: {}", path);
        logger.info("ステータスコード: {} {}", statusCode, statusText);
        logger.info("処理時間: {}ms", processingTime);
        
        // 処理時間に基づく警告
        if (processingTime > 5000) {
            logger.warn("処理時間が長い可能性があります: {}ms", processingTime);
        }
        
        // デバッグレベルでレスポンスヘッダー情報も出力
        if (logger.isDebugEnabled()) {
            logger.debug("レスポンスヘッダー:");
            response.getHeaders().forEach((name, values) -> 
                logger.debug("  {}: {}", name, String.join(", ", values))
            );
        }
        
        logger.info("========================");
    }

    /**
     * リモートアドレスを取得する
     * 
     * @param request HTTPリクエスト
     * @return リモートアドレス
     */
    private String getRemoteAddress(ServerHttpRequest request) {
        // X-Forwarded-Forヘッダーをチェック（プロキシ経由の場合）
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 最初のIPアドレスを取得（複数ある場合はカンマ区切り）
            return xForwardedFor.split(",")[0].trim();
        }
        
        // X-Real-IPヘッダーをチェック
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // 直接接続の場合はリモートアドレスを使用
        return request.getRemoteAddress() != null 
            ? request.getRemoteAddress().getAddress().getHostAddress() 
            : "unknown";
    }

    /**
     * フィルター設定クラス
     */
    public static class Config {
        /**
         * デバッグログレベルでの詳細出力を有効にするかどうか
         */
        private boolean enableDebugLogging = false;

        public boolean isEnableDebugLogging() {
            return enableDebugLogging;
        }

        public void setEnableDebugLogging(boolean enableDebugLogging) {
            this.enableDebugLogging = enableDebugLogging;
        }
    }
}