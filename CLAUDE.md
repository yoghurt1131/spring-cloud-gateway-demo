# Spring Cloud Gateway デモアプリケーション 仕様書

## プロジェクト概要

Spring Cloud Gatewayを使用したデモアプリケーションを作成してください。このアプリケーションは、Gatewayが自身のバックエンドエンドポイントにプロキシする構成で、FilterでリクエストパラメータやヘッダーT情報を補完する機能を実装します。

## 技術スタック

- **言語**: Java 17以上
- **フレームワーク**: Spring Boot 3.x
- **Gateway**: Spring Cloud Gateway
- **ビルドツール**: Maven または Gradle
- **パッケージ構成**: 単一のSpring Bootアプリケーション

## アプリケーション構成

### 1. ポート設定
- Gateway: `8080`
- Backend API: 同一アプリケーション内で `8080` (Gatewayと同じポート)

### 2. エンドポイント設計

#### バックエンドエンドポイント
**パス**: `/api/user-info`
**メソッド**: GET
**クエリパラメータ**:
- `userId` (必須): ユーザーID
- `userName` (オプション): ユーザー名
- `timestamp` (オプション): リクエストタイムスタンプ
- `source` (オプション): リクエスト元

**レスポンス例**:
```json
{
  "userId": "12345",
  "userName": "John Doe",
  "timestamp": "2025-01-15T10:30:00Z",
  "source": "gateway",
  "processedBy": "backend-api",
  "message": "User information retrieved successfully"
}
```

#### Gatewayエンドポイント
**パス**: `/gateway/user-info`
**メソッド**: GET
**クエリパラメータ**:
- `userId` (必須): ユーザーID

## 実装要件

### 1. Gatewayルーティング設定

`application.yml` で以下のルーティングを設定:
- パス `/gateway/**` へのリクエストを `/api/**` にルーティング
- 例: `/gateway/user-info` → `/api/user-info`

### 2. カスタムGatewayFilter実装

以下の機能を持つカスタムFilterを実装してください:

#### パラメータ補完Filter
**クラス名**: `ParameterEnrichmentGatewayFilterFactory`

**機能**:
1. `userName` パラメータが存在しない場合、デフォルト値を追加
   - デフォルト値: "Anonymous User"
2. `timestamp` パラメータが存在しない場合、現在のISO8601形式のタイムスタンプを追加
3. `source` パラメータに "gateway" を設定
4. リクエストヘッダーに `X-Gateway-Processed: true` を追加

#### ログ出力Filter
**クラス名**: `RequestLoggingGatewayFilterFactory`

**機能**:
1. リクエスト受信時にログ出力
   - パス、メソッド、元のクエリパラメータ
2. レスポンス送信時にログ出力
   - ステータスコード、処理時間

### 3. バックエンドコントローラー実装

**クラス名**: `UserInfoController`

**機能**:
1. `/api/user-info` エンドポイントを提供
2. すべてのクエリパラメータを受け取る
3. 受け取ったパラメータを含むJSONレスポンスを返す
4. パラメータが補完されていることを確認できるレスポンスを構築

### 4. 設定ファイル

#### application.yml の要件
```yaml
server:
  port: 8080

spring:
  application:
    name: spring-cloud-gateway-demo
  cloud:
    gateway:
      routes:
        - id: user-info-route
          uri: http://localhost:8080
          predicates:
            - Path=/gateway/**
          filters:
            - StripPrefix=1
            - ParameterEnrichment
            - RequestLogging

logging:
  level:
    org.springframework.cloud.gateway: DEBUG
```

## 動作確認方法

### テストシナリオ1: 最小限のパラメータ
```bash
curl "http://localhost:8080/gateway/user-info?userId=12345"
```

**期待される動作**:
- Filterが `userName`, `timestamp`, `source` を自動追加
- バックエンドで補完されたパラメータを含むレスポンスを返す

### テストシナリオ2: すべてのパラメータ指定
```bash
curl "http://localhost:8080/gateway/user-info?userId=12345&userName=Alice"
```

**期待される動作**:
- 指定された `userName` はそのまま使用
- `timestamp` と `source` は自動追加
- レスポンスに全パラメータが反映される

### テストシナリオ3: 直接バックエンド呼び出し
```bash
curl "http://localhost:8080/api/user-info?userId=12345"
```

**期待される動作**:
- Filterを経由しないため、パラメータ補完なし
- 指定されたパラメータのみでレスポンスを返す

## プロジェクト構造

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── gateway/
│   │               ├── GatewayDemoApplication.java
│   │               ├── controller/
│   │               │   └── UserInfoController.java
│   │               ├── filter/
│   │               │   ├── ParameterEnrichmentGatewayFilterFactory.java
│   │               │   └── RequestLoggingGatewayFilterFactory.java
│   │               └── model/
│   │                   └── UserInfoResponse.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/
            └── example/
                └── gateway/
                    └── GatewayDemoApplicationTests.java
```

## 追加要件

1. **エラーハンドリング**: `userId` が指定されていない場合は400エラーを返す
2. **README.md**: プロジェクトのビルド方法、実行方法、テスト方法を記載
3. **依存関係**: pom.xml (または build.gradle) に必要な依存関係を明記
4. **コメント**: 各クラスと主要メソッドに日本語でJavadocコメントを追加

## 成功基準

- アプリケーションが正常に起動する
- 上記3つのテストシナリオがすべて期待通りに動作する
- ログにFilter処理の内容が出力される
- コードが適切に構造化され、コメントが記載されている

# アプリケーション実行コマンド

## 起動
```bash
mvn spring-boot:run
```

## テストコマンド

### テストシナリオ1: 最小限のパラメータ（フィルターでパラメータ補完）
```bash
curl "http://localhost:8080/gateway/user-info?userId=12345"
```

### テストシナリオ2: 一部パラメータ指定（残りはフィルターで補完）
```bash
curl "http://localhost:8080/gateway/user-info?userId=12345&userName=Alice"
```

### テストシナリオ3: 直接バックエンド呼び出し（フィルター処理なし）
```bash
curl "http://localhost:8080/api/user-info?userId=12345"
```

### 追加エンドポイント
```bash
curl "http://localhost:8080/api/info"
curl "http://localhost:8080/api/health"
```

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.
