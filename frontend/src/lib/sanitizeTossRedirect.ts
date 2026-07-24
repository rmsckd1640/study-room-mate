// Toss 결제위젯이 successUrl/failUrl로 리다이렉트할 때, orderId/paymentKey/amount 같은 표준
// 파라미터는 해시(#) 앞의 진짜 쿼리스트링에 붙이지만, paymentType 등 일부는 `/` 구분자 없이
// 해시 뒤에 `&`로 그대로 이어붙인다 (예: "#/user/payment/success&paymentType=NORMAL").
// 해시라우터는 `#` 뒤 문자열에 `?`가 없으면 전체를 pathname으로 취급하므로, 이 상태로는
// 등록된 라우트("/user/payment/success")와 매칭되지 않아 404가 난다.
// 라우터(createHashRouter)가 최초 위치를 읽기 전에 해시를 정리해야 하므로, 이 모듈은
// main.tsx에서 App(및 routes.ts)보다 먼저 import되어야 한다.
const match = window.location.hash.match(/^#(\/user\/payment\/(?:success|fail))(?:&.*)?$/)
if (match) {
  window.location.hash = match[1]
}
