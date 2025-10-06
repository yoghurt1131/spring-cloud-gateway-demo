package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gatewayデモアプリケーションのメインクラス
 * 
 * このアプリケーションは、Spring Cloud Gatewayを使用して、
 * 自身のバックエンドエンドポイントにプロキシする構成で動作します。
 * カスタムフィルターによってリクエストパラメータやヘッダー情報を補完します。
 * 
 * @author Claude
 * @version 1.0.0
 */
@SpringBootApplication
public class GatewayDemoApplication {

    /**
     * アプリケーションのエントリーポイント
     * 
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayDemoApplication.class, args);
    }
}