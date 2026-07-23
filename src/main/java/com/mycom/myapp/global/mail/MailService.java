package com.mycom.myapp.global.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailService {

    // 실제 SMTP 발송을 담당하는 Spring 표준 인터페이스 (application-local.properties의 spring.mail.* 설정으로 자동 구성됨)
    private final JavaMailSender mailSender;

    // 재설정 링크의 도메인 부분 - 프론트 주소가 바뀌어도 코드 수정 없이 설정값만 바꾸면 되도록 외부화
    @Value("${app.password-reset-url}")
    private String passwordResetUrl;

    // 비밀번호 재설정 링크(토큰 포함)가 담긴 메일을 발송
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[StudyRoomMate] 비밀번호 재설정 안내");
        message.setText("아래 링크에서 비밀번호를 재설정해주세요.\n\n"
                + passwordResetUrl + "?token=" + token
                + "\n\n이 링크는 30분간 유효합니다.");

        mailSender.send(message);
    }
}
