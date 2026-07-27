package com.mycom.myapp.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// @Async를 붙인 메서드가 별도 스레드에서 실행되도록 활성화 (안 켜면 @Async는 무시되고 그냥 동기 실행됨)
@Configuration
@EnableAsync
public class AsyncConfig {
}
