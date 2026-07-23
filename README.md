# study-room-mate
스터디룸/공간 예약 통합 관리 시스템

## 기술 스택
- Java 21, Spring Boot 4.1
- Spring Data JPA, Spring Security, JWT (jjwt)
- MySQL
- Gradle

## 요구 사항
- JDK 21
- MySQL 8.x (로컬에 `studyroommate` 데이터베이스)

## 로컬 실행 방법

### 1. 데이터베이스 생성 및 스키마 적용
```bash
mysql -u root -p -e "CREATE DATABASE studyroommate CHARACTER SET utf8mb4;"
mysql -u root -p studyroommate < src/main/resources/db/migration/0001_init_schema.up.sql
```
테스트용 더미 데이터가 필요하면 추가로 적용한다.
```bash
mysql -u root -p studyroommate < src/main/resources/db/migration/0002_sample_data.sql
```

### 2. 로컬 환경설정 파일 생성
`application-local.properties.example`을 복사해서 값을 채워넣는다.
```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

| 설정 | 설명 |
|---|---|
| `spring.datasource.*` | MySQL 접속 정보 |
| `jwt.secret` | JWT 서명 키 |
| `toss.payment.toss-secret-key` | Toss Payments 시크릿 키 |
| `spring.mail.*` | Gmail 발신 계정 (2단계 인증 후 앱 비밀번호 발급) |
| `app.password-reset-url` | 비밀번호 재설정 메일에 삽입되는 프론트엔드 주소 |

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```
기본적으로 `local` 프로필로 구동되며 (`application.properties`의 `spring.profiles.active=local`), `http://localhost:8080`에서 확인할 수 있다.

### API 문서
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 테스트
```bash
./gradlew test
```
JaCoCo 커버리지 리포트는 `build/reports/jacoco/test/html/index.html`에 생성된다.
