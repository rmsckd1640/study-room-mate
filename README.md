# study-room-mate
스터디룸/공간 예약 통합 관리 시스템

## 구성
- `backend/` — Java 21, Spring Boot 4.1 (Spring Data JPA, Spring Security, JWT, MySQL, Gradle)
- `frontend/` — React + Vite, GitHub Pages로 배포됨

## 요구 사항
- JDK 21
- MySQL 8.x (로컬에 `studyroommate` 데이터베이스)

## 로컬 실행 방법 (backend)

### 1. 데이터베이스 생성 및 스키마 적용
```bash
mysql -u root -p -e "CREATE DATABASE studyroommate CHARACTER SET utf8mb4;"
mysql -u root -p studyroommate < backend/src/main/resources/db/migration/0001_init_schema.up.sql
```
테스트용 더미 데이터가 필요하면 추가로 적용한다.
```bash
mysql -u root -p studyroommate < backend/src/main/resources/db/migration/0002_sample_data.sql
```

### 2. 로컬 환경설정 파일 생성
`application-local.properties.example`을 복사해서 값을 채워넣는다.
```bash
cp backend/src/main/resources/application-local.properties.example backend/src/main/resources/application-local.properties
```

| 설정 | 설명 |
|---|---|
| `spring.datasource.*` | MySQL 접속 정보 |
| `jwt.secret` | JWT 서명 키 |
| `toss.payment.toss-secret-key` | Toss Payments 시크릿 키. 프론트가 결제위젯 SDK(`test_gck_...`)로 연동돼 있어서 **반드시 짝이 맞는 위젯 연동 시크릿 키**를 써야 한다 (구버전 `test_sk_...` 개별 연동 키는 `NotSupportedAPIIndividualKeyError`로 실패함). 회원가입 전 테스트용: `test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6` |
| `spring.mail.*` | Gmail 발신 계정 (2단계 인증 후 앱 비밀번호 발급) |
| `app.password-reset-url` | 비밀번호 재설정 메일에 삽입되는 프론트엔드 주소 |

### 3. 애플리케이션 실행
```bash
cd backend
./gradlew bootRun
```
기본적으로 `local` 프로필로 구동되며 (`application.properties`의 `spring.profiles.active=local`), `http://localhost:8080`에서 확인할 수 있다.

API 문서(Swagger UI): `http://localhost:8080/swagger-ui/index.html`

### 4. 프론트엔드에서 테스트
백엔드만 로컬에서 띄우면 된다. 프론트엔드는 GitHub Pages에 이미 배포돼 있고, `http://localhost:8080`을 호출하도록 빌드되어 있다 (백엔드 `SecurityConfig`의 CORS 설정도 이 배포 주소를 허용하도록 되어 있음).

백엔드가 8080 포트로 떠 있는 상태에서 아래 주소를 열어 테스트한다.

**https://rmsckd1640.github.io/study-room-mate/**

## 테스트
```bash
cd backend
./gradlew test
```
JaCoCo 커버리지 리포트는 `backend/build/reports/jacoco/test/html/index.html`에 생성된다.
