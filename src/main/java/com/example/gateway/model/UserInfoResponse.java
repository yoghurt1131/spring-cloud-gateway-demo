package com.example.gateway.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ユーザー情報レスポンスを表すモデルクラス
 * 
 * バックエンドAPIからのレスポンス用のデータ構造を定義します。
 * Gatewayフィルターによって補完されたパラメータを含む情報が格納されます。
 * 
 * @author Claude
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {

    /**
     * ユーザーID（必須）
     */
    private String userId;

    /**
     * ユーザー名（オプション、フィルターによって補完される可能性あり）
     */
    private String userName;

    /**
     * リクエストタイムスタンプ（フィルターによって補完される）
     */
    private String timestamp;

    /**
     * リクエスト元（フィルターによって設定される）
     */
    private String source;

    /**
     * 処理者情報
     */
    private String processedBy;

    /**
     * メッセージ
     */
    private String message;

    /**
     * デフォルトコンストラクタ
     */
    public UserInfoResponse() {
    }

    /**
     * 全パラメータを指定するコンストラクタ
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param timestamp タイムスタンプ
     * @param source リクエスト元
     * @param processedBy 処理者
     * @param message メッセージ
     */
    public UserInfoResponse(String userId, String userName, String timestamp, 
                           String source, String processedBy, String message) {
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.source = source;
        this.processedBy = processedBy;
        this.message = message;
    }

    // Getter and Setter methods

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", source='" + source + '\'' +
                ", processedBy='" + processedBy + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}