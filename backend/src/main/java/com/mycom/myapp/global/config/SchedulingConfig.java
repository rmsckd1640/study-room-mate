package com.mycom.myapp.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// @Scheduled가 붙은 메서드가 실제로 주기 실행되도록 활성화 (안 켜면 @Scheduled는 무시됨)
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
