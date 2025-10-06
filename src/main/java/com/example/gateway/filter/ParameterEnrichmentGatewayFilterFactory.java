package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;

/**
 * パラメータ補完を行うGatewayフィルターファクトリー
 * 
 * このフィルターは以下の機能を提供します：
 * 1. userNameパラメータが存在しない場合、デフォルト値を追加
 * 2. timestampパラメータが存在しない場合、現在のISO8601形式のタイムスタンプを追加
 * 3. sourceパラメータに "gateway" を設定
 * 4. リクエストヘッダーに X-Gateway-Processed: true を追加
 * 
 * @author Claude
 * @version 1.0.0
 */
@Component
public class ParameterEnrichmentGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<ParameterEnrichmentGatewayFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(ParameterEnrichmentGatewayFilterFactory.class);

    /**
     * デフォルトのユーザー名
     */
    private static final String DEFAULT_USER_NAME = "AnonymousUser";

    /**
     * Gatewayソース識別子
     */
    private static final String GATEWAY_SOURCE = "gateway";

    /**
     * Gateway処理済みヘッダー
     */
    private static final String GATEWAY_PROCESSED_HEADER = "X-Gateway-Processed";

    public ParameterEnrichmentGatewayFilterFactory() {
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
            logger.debug("ParameterEnrichmentFilter: リクエスト処理開始");

            // 元のリクエストURIを取得
            URI originalUri = exchange.getRequest().getURI();
            logger.debug("元のURI: {}", originalUri);

            // 現在のクエリパラメータを取得（変更可能なコピーを作成）
            MultiValueMap<String, String> queryParams = new org.springframework.util.LinkedMultiValueMap<>();
            MultiValueMap<String, String> originalParams = UriComponentsBuilder.fromUri(originalUri).build().getQueryParams();
            queryParams.addAll(originalParams);

            // パラメータ補完処理
            boolean modified = false;

            // userNameパラメータの補完
            if (!queryParams.containsKey("userName")) {
                queryParams.add("userName", DEFAULT_USER_NAME);
                modified = true;
                logger.debug("userNameパラメータを補完: {}", DEFAULT_USER_NAME);
            }

            // timestampパラメータの補完
            if (!queryParams.containsKey("timestamp")) {
                String currentTimestamp = Instant.now().toString();
                queryParams.add("timestamp", currentTimestamp);
                modified = true;
                logger.debug("timestampパラメータを補完: {}", currentTimestamp);
            }

            // sourceパラメータの設定（常に上書き）
            queryParams.set("source", GATEWAY_SOURCE);
            modified = true;
            logger.debug("sourceパラメータを設定: {}", GATEWAY_SOURCE);

            if (modified) {
                // 新しいURIを構築（URLエンコーディング有効）
                URI newUri = UriComponentsBuilder.fromUri(originalUri)
                        .replaceQueryParams(queryParams)
                        .encode()
                        .build()
                        .toUri();

                logger.info("パラメータ補完後のURI: {}", newUri);

                // 新しいリクエストを作成
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .uri(newUri)
                                .header(GATEWAY_PROCESSED_HEADER, "true")
                                .build())
                        .build();

                logger.debug("ParameterEnrichmentFilter: 処理完了");
                return chain.filter(modifiedExchange);
            } else {
                // パラメータの変更がない場合はヘッダーのみ追加
                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(exchange.getRequest().mutate()
                                .header(GATEWAY_PROCESSED_HEADER, "true")
                                .build())
                        .build();

                logger.debug("ParameterEnrichmentFilter: ヘッダーのみ追加");
                return chain.filter(modifiedExchange);
            }
        };
    }

    /**
     * フィルター設定クラス
     */
    public static class Config {
        // 現在は設定項目なし、将来の拡張のために用意
    }
}