package com.example.gateway.handler;

import com.example.gateway.model.UserInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * ユーザー情報を提供するWebFluxハンドラー
 * 
 * このハンドラーは、/api/user-info エンドポイントを提供し、
 * Gatewayからのリクエストを処理します。フィルターによって補完された
 * パラメータを含むレスポンスを返します。
 * 
 * @author Claude
 * @version 1.0.0
 */
@Component
public class UserInfoHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoHandler.class);

    /**
     * ユーザー情報を取得するハンドラーメソッド
     * 
     * このメソッドは、Gatewayフィルターを通過したリクエストを処理し、
     * 補完されたパラメータを含むユーザー情報レスポンスを返します。
     * 
     * @param request サーバーリクエスト
     * @return ユーザー情報レスポンス
     */
    public Mono<ServerResponse> getUserInfo(ServerRequest request) {
        // クエリパラメータを取得
        String userId = request.queryParam("userId").orElse(null);
        String userName = request.queryParam("userName").orElse(null);
        String timestamp = request.queryParam("timestamp").orElse(null);
        String source = request.queryParam("source").orElse(null);

        logger.info("バックエンドAPIに到達しました - userId: {}", userId);
        logger.debug("受信したパラメータ: userId={}, userName={}, timestamp={}, source={}", 
                    userId, userName, timestamp, source);

        // userIdが空の場合はバリデーションエラー
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("userIdが指定されていません");
            return ServerResponse.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("error", "userId is required"));
        }

        // レスポンスオブジェクトを構築
        UserInfoResponse response = new UserInfoResponse(
                userId,
                userName,
                timestamp,
                source,
                "backend-api",
                "User information retrieved successfully"
        );

        logger.info("レスポンス作成完了: {}", response);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(response);
    }

    /**
     * ヘルスチェック用のハンドラーメソッド
     * 
     * @param request サーバーリクエスト
     * @return ヘルスチェック結果
     */
    public Mono<ServerResponse> health(ServerRequest request) {
        Map<String, String> healthResponse = Map.of(
                "status", "UP",
                "service", "backend-api",
                "timestamp", java.time.Instant.now().toString()
        );

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(healthResponse);
    }
}