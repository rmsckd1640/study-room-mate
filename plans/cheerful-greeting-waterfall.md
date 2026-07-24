# StudyRoomMate 반응형 프론트엔드 확장 계획

## Context

현재 앱은 기능적으로 작동하지만 세 가지 주요 문제가 있다.
1. **모바일 미대응** — 사이드바 레이아웃이 데스크톱 전용이고, 테이블은 모바일에서 잘린다.
2. **화면 누락** — 룸 상세 페이지, 전용 위시리스트/예약내역 페이지가 없다.
3. **공통 UI 부재** — 로딩/빈/에러 상태, 토스트 알림이 없고 동일 컴포넌트가 여러 파일에 중복된다.

이 플랜은 기존 동작하는 코드를 깨지 않으면서 위 세 가지를 해결한다.

---

## 구현 순서 (Phase별)

### Phase 1 — 공통 UI 프리미티브 (새 파일만, 기존 파일 미수정)

**`src/components/ui/LoadingSpinner.tsx`** (named export)
- props: `size?: 'sm'|'md'|'lg'`, `label?: string`
- `animate-spin` SVG 링, 색상 `#1e3a5f`

**`src/components/ui/EmptyState.tsx`** (named export)
- props: `icon?: ReactNode`, `title: string`, `description?: string`, `action?: {label, onClick}`
- 64px 아이콘 슬롯 + 제목 + 설명 + CTA 버튼 (네이비 그라데이션 기존 패턴 동일)

**`src/components/ui/ErrorState.tsx`** (named export)
- props: `message?: string`, `onRetry?: () => void`
- 빨간 아이콘 + 메시지 + 재시도 버튼

**`src/components/ui/Badge.tsx`** (named export)
- props: `variant: 'grade'|'reservationStatus'|'roomStatus'`, `value: string`
- `grade`: `bronze/silver/gold/platinum` → `GRADE_CONFIG` 색상, 라벨
- `reservationStatus`: `pending→대기 중`, `confirmed→확정`, `cancelled→취소됨`, `payment_done→결제 완료`, `rejected→거절됨`
- `roomStatus`: 기존 `StatusBadge` 대체

**`src/components/rooms/StarRating.tsx`** (named exports: `StarRating`, `StarPicker`)
- 기존 `AdminRoomsPage`, `MyPage`, `RoomReviewsPage`에 중복된 구현을 하나로 추출

**`src/context/ToastContext.tsx`**
- 타입: `Toast = { id: string; type: 'success'|'error'; message: string }`
- exports: `ToastProvider`, `useToast` (`showToast(message, type)`)
- 3초 자동 소멸, 우하단 고정 (`fixed bottom-4 right-4 z-[9999]`), 모바일은 하단 중앙
- 성공: `#f0fdf4` bg + `#16a34a`, 에러: `#fef2f2` bg + `#b91c1c`

---

### Phase 2 — 타입 시스템 수정 (`AuthContext.tsx` only)

**`src/context/AuthContext.tsx`** 변경 사항 (additive, 기존 호출부 깨지지 않음):
1. `ReservationStatus` 유니온에 `'payment_done'` 추가
2. `Reservation` 타입에 `cancelReason?: string` 추가
3. `cancelReservation(id: number, reason?: string): void` 시그니처 업데이트
4. `deleteAccount(): void` 메서드 추가 (모든 상태 초기화 후 `logout()`)
5. `GRADE_CONFIG['platinum'].label` → `'VIP'` (요구사항의 BRONZE/SILVER/GOLD/VIP 매핑)

---

### Phase 3 — 새 페이지 3개 (기존 파일 미수정)

**`src/pages/user/RoomDetailPage.tsx`** → `/user/rooms/:roomId`
- 뒤로가기 버튼 → `/user/rooms`
- Hero 카드: 방 이름, 수용인원, 정가/할인가, 상태 Badge, 위시리스트 토글(★)
- 설명 섹션
- 시간대 현황: `TimeSlotRow` (AdminRoomsPage에서 named export 추가 후 import)
- 평점 요약 + 리뷰 리스트 (`reviews.filter(r => r.roomId === id)`)
- 하단 CTA: "예약하기" → `/user/rooms/:roomId/reserve`
- 로딩/없는방 상태: `LoadingSpinner`, `EmptyState`

**`src/pages/user/WishlistPage.tsx`** → `/user/wishlist`
- 헤더: 위시리스트 + 개수
- 3컬럼 데스크톱 / 1컬럼 모바일 카드 그리드
- 각 카드: 방 이름(상세 링크), 수용인원, 가격, 취소특가 뱃지, 예약하기, 하트 제거 버튼
- 빈 상태: `EmptyState` (하트 아이콘, `/user/rooms` CTA)

**`src/pages/user/ReservationHistoryPage.tsx`** → `/user/reservations`
- 상태 필터 탭: `전체|대기 중|확정|결제 완료|취소됨|거절됨` (가로 스크롤 pill)
- 리스트: 테이블 대신 카드 (모바일 친화적) — 방 이름, 날짜, 시간, 금액, 상태 Badge
- `confirmed` 상태에만 "취소하기" 버튼 → 사유 입력 모달 (`<textarea>` + 확인/취소)
- 취소 완료 시 `showToast('예약이 취소되었습니다.', 'success')`
- 취소된 예약에 "룸 보기" 링크 → `/user/rooms/:roomId`
- 각 상태별 `EmptyState`

---

### Phase 4 — 라우트 등록

**`src/routes.ts`** — `/user` children에 3개 추가:
```ts
{ path: 'rooms/:roomId', Component: RoomDetailPage },     // 기존 rooms/:roomId/reviews보다 먼저 선언
{ path: 'wishlist', Component: WishlistPage },
{ path: 'reservations', Component: ReservationHistoryPage },
```
> React Router v8은 세그먼트 수 기준으로 더 구체적인 패스를 우선 매칭하므로 `rooms/:roomId`와 `rooms/:roomId/reviews`는 충돌 없음.

---

### Phase 5 — 기존 파일 업데이트

**`src/pages/user/UserLayout.tsx`** — 모바일 반응형

데스크톱(md+): 기존 240px 좌측 사이드바 유지  
모바일(~md): 사이드바 숨김, 하단 고정 네비 추가

```
// 모바일 하단 네비 구조 (fixed bottom-0, z-40, bg-white border-t)
// 아이콘 + 라벨 4개: 스터디룸 | 위시리스트 | 예약 | 마이페이지
// 활성 탭: #1e3a5f 색상
```

- 메인 영역 하단 패딩: `pb-16 md:pb-0` (하단 네비 높이 보정)
- 사이드바 `navItems`: 기존 2개 → 4개 (rooms, wishlist, reservations, mypage) 추가
- 헤더 breadcrumb: `useLocation()` 기반 동적 라벨

**`src/pages/admin/AdminRoomsPage.tsx`** — 모바일 반응형 + named export 추가

1. `export { TimeSlotRow, CancelSaleCountdown }` named export 추가 (RoomDetailPage 임포트용)
2. 통계 그리드: `grid-cols-3` → `grid-cols-1 sm:grid-cols-3`
3. 필터 바: `flex-wrap gap-3` (이미 존재) 확인 후 필드 `min-w-[160px]` 보장
4. 리스트 뷰: `md:block` 테이블 + `md:hidden` 카드 스택 (갤러리 `RoomCard` 재사용)
5. 갤러리 뷰: `grid-cols-1 sm:grid-cols-2 lg:grid-cols-3` (이미 존재)

**`src/pages/admin/MyPage.tsx`** — 탭 간소화 + 프로필 확장

- `tab` 타입: `'profile' | 'reviews'` (favorites/reservations 탭 제거)
- 프로필 탭 상단에 Quick Links 카드: "예약 내역 →" `/user/reservations`, "위시리스트 →" `/user/wishlist`
- 등급 Badge 표시 (공유 `Badge` 컴포넌트 사용)
- "비밀번호 변경" 서브섹션 추가 (현재 비밀번호 + 새 비밀번호 필드, 클라이언트 검증만)
- "회원 탈퇴" 위험 구역 + 확인 모달 → `deleteAccount()` 호출

**`src/pages/user/PaymentPage.tsx`** — 결제 완료 후 이동 경로
- done 화면의 "마이페이지" 버튼 → "예약 내역" (`/user/reservations`)

**`src/pages/admin/RoomReviewsPage.tsx`** — 중복 제거
- 인라인 `StarPicker`, `StarDisplay` → `src/components/rooms/StarRating.tsx` import로 교체

**`src/App.tsx`** — ToastProvider 추가
- `<AuthProvider><ToastProvider><RouterProvider/></ToastProvider></AuthProvider>`

---

### Phase 6 — 토스트 연동

기존 페이지에 `useToast()` 연결:
- `MyPage`: 프로필 저장 → `showToast('프로필이 저장되었습니다.', 'success')`
- `RoomReviewsPage`: 리뷰 등록 → `showToast('리뷰가 등록되었습니다.', 'success')`
- `ReservationHistoryPage`: 취소 완료 → `showToast('예약이 취소되었습니다.', 'success')`

---

## 파일 목록

### 새로 생성 (11개)
| 경로 | 내용 |
|------|------|
| `src/components/ui/LoadingSpinner.tsx` | 로딩 스피너 |
| `src/components/ui/EmptyState.tsx` | 빈 상태 |
| `src/components/ui/ErrorState.tsx` | 에러 상태 |
| `src/components/ui/Badge.tsx` | 등급/예약상태/룸상태 뱃지 |
| `src/components/rooms/StarRating.tsx` | StarRating + StarPicker |
| `src/context/ToastContext.tsx` | 토스트 알림 시스템 |
| `src/pages/user/RoomDetailPage.tsx` | 룸 상세 |
| `src/pages/user/WishlistPage.tsx` | 위시리스트 |
| `src/pages/user/ReservationHistoryPage.tsx` | 예약 내역 |

### 수정 (9개)
| 경로 | 변경 내용 |
|------|----------|
| `src/context/AuthContext.tsx` | payment_done 추가, cancelReason, deleteAccount |
| `src/routes.ts` | 3개 라우트 추가 |
| `src/App.tsx` | ToastProvider 감싸기 |
| `src/pages/user/UserLayout.tsx` | 모바일 하단 네비 + navItems 2개 추가 |
| `src/pages/admin/AdminRoomsPage.tsx` | named exports + 모바일 반응형 |
| `src/pages/admin/MyPage.tsx` | 탭 간소화 + 프로필 확장 |
| `src/pages/admin/RoomReviewsPage.tsx` | StarRating/StarPicker 공유 컴포넌트로 교체 |
| `src/pages/user/PaymentPage.tsx` | 결제 완료 후 이동 경로 수정 |
| `src/pages/admin/AdminLayout.tsx` | (미정) 필요시 모바일 반응형 |

### 변경 없음
- `src/index.css`, `src/main.tsx`
- `src/pages/admin/DashboardPage.tsx`
- `src/pages/admin/ReservationManagePage.tsx`
- `src/pages/user/ReservePage.tsx`
- 인증 페이지들 (이미 모바일 친화적)

---

## 검증 방법

1. **TypeScript**: `pnpm tsc --noEmit` — 에러 0개
2. **라우팅**: 각 신규 URL(`#/user/rooms/1`, `#/user/wishlist`, `#/user/reservations`) 직접 접근 확인
3. **반응형**: 브라우저 DevTools 375px(모바일) 뷰에서 bottom nav 표시, 컨텐츠 clipping 없음 확인
4. **토스트**: 리뷰 등록/취소 시 3초 토스트 노출 확인
5. **빈 상태**: 위시리스트 0개, 예약 내역 0개, 각 상태 필터 결과 0개일 때 EmptyState 표시 확인
6. **예약 취소 플로우**: 확정 예약 → 취소 모달 → 사유 입력 → 취소됨 상태로 변경 → 토스트 확인
