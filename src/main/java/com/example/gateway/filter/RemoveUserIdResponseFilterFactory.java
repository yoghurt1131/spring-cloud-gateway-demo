package com.example.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * レスポンスボディからuserIdフィールドを削除するGatewayフィルターファクトリー
 * 
 * このフィルターは、ModifyResponseBodyGatewayFilterFactoryを使用して
 * レスポンスJSONからuserIdフィールドを除去します。
 * 
 * @author Claude
 * @version 1.0.0
 */
@Component
public class RemoveUserIdResponseFilterFactory extends AbstractGatewayFilterFactory<RemoveUserIdResponseFilterFactory.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RemoveUserIdResponseFilterFactory.class);
    
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;
    private final ObjectMapper objectMapper;

    public RemoveUserIdResponseFilterFactory(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter) {
        super(Config.class);
        this.modifyResponseBodyFilter = modifyResponseBodyFilter;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return modifyResponseBodyFilter.apply(c -> c.setRewriteFunction(String.class, String.class, new UserIdRemovalRewriteFunction()));
    }

    /**
     * userIdフィールドを削除するRewriteFunction実装
     */
    private class UserIdRemovalRewriteFunction implements RewriteFunction<String, String> {
        
        @Override
        public Publisher<String> apply(ServerWebExchange exchange, String originalResponseBody) {
            try {
                logger.debug("元のレスポンスボディ: {}", originalResponseBody);
                
                // JSONをパース
                JsonNode jsonNode = objectMapper.readTree(originalResponseBody);
                
                // ObjectNodeの場合のみuserIdを削除
                if (jsonNode.isObject()) {
                    ObjectNode objectNode = (ObjectNode) jsonNode;
                    
                    // userIdフィールドが存在する場合は削除
                    if (objectNode.has("userId")) {
                        objectNode.remove("userId");
                        logger.info("userIdフィールドを削除しました");
                    } else {
                        logger.debug("userIdフィールドが見つかりませんでした");
                    }
                    
                    // 修正されたJSONを文字列に変換
                    String modifiedResponseBody = objectMapper.writeValueAsString(objectNode);
                    logger.debug("修正後のレスポンスボディ: {}", modifiedResponseBody);
                    
                    return Mono.just(modifiedResponseBody);
                } else {
                    // オブジェクトでない場合はそのまま返す
                    logger.debug("レスポンスボディがJSONオブジェクトではありません");
                    return Mono.just(originalResponseBody);
                }
                
            } catch (JsonProcessingException e) {
                logger.error("JSONパースエラー: {}", e.getMessage());
                // エラーの場合は元のレスポンスボディをそのまま返す
                return Mono.just(originalResponseBody);
            } catch (Exception e) {
                logger.error("予期せぬエラー: {}", e.getMessage(), e);
                return Mono.just(originalResponseBody);
            }
        }
    }

    /**
     * フィルター設定クラス
     */
    public static class Config {
        /**
         * デバッグログを有効にするかどうか
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