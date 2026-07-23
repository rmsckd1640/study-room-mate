-- 샘플 더미 데이터 (테스트용)
-- password는 BCryptPasswordEncoder(cost 10)로 'password123!'를 인코딩한 값 -> 모든 계정 로그인 시 password123! 사용

INSERT INTO member (id, username, password, email, name, role, grade) VALUES
(1, 'admin01', '$2y$10$7k.dsIhIGgVkNgmc5MWVr.1CGlCqjs.jBs7oR9eZDlaETKule1Rzm', 'admin@test.com', '관리자', 'ADMIN', 'BRONZE'),
(2, 'user01', '$2y$10$7k.dsIhIGgVkNgmc5MWVr.1CGlCqjs.jBs7oR9eZDlaETKule1Rzm', 'user01@test.com', '김민준', 'USER', 'GOLD'),
(3, 'user02', '$2y$10$7k.dsIhIGgVkNgmc5MWVr.1CGlCqjs.jBs7oR9eZDlaETKule1Rzm', 'user02@test.com', '이서연', 'USER', 'SILVER'),
(4, 'user03', '$2y$10$7k.dsIhIGgVkNgmc5MWVr.1CGlCqjs.jBs7oR9eZDlaETKule1Rzm', 'user03@test.com', '박지훈', 'USER', 'BRONZE'),
(5, 'user04', '$2y$10$7k.dsIhIGgVkNgmc5MWVr.1CGlCqjs.jBs7oR9eZDlaETKule1Rzm', 'user04@test.com', '최유리', 'USER', 'VIP');

INSERT INTO room (id, name, capacity, price) VALUES
(1, '1인실 A', 1, 3000),
(2, '2인실 B', 2, 5000),
(3, '4인실 스터디룸 C', 4, 9000),
(4, '세미나룸 D', 8, 15000),
(5, '프리미엄룸 E', 6, 20000);

INSERT INTO reservation (id, member_id, room_id, reservation_date, start_time, end_time, status) VALUES
(1, 2, 1, '2026-07-20', '2026-07-20 09:00:00', '2026-07-20 11:00:00', 'CONFIRMED'),
(2, 2, 3, '2026-07-21', '2026-07-21 13:00:00', '2026-07-21 15:00:00', 'PAYMENT_DONE'),
(3, 3, 2, '2026-07-22', '2026-07-22 10:00:00', '2026-07-22 12:00:00', 'PENDING'),
(4, 4, 4, '2026-07-18', '2026-07-18 14:00:00', '2026-07-18 16:00:00', 'CANCELLED'),
(5, 5, 5, '2026-07-25', '2026-07-25 09:00:00', '2026-07-25 13:00:00', 'PAYMENT_DONE'),
(6, 3, 1, '2026-07-15', '2026-07-15 09:00:00', '2026-07-15 10:00:00', 'REJECTED');

INSERT INTO payment (id, amount, approved_at, created_at, order_id, payment_key, status, reservation_id) VALUES
(1, 18000, '2026-07-21 13:00:00', '2026-07-21 12:59:00', 'ORDER_20260721_0001', 'pay_key_0001', 'DONE', 2),
(2, 80000, '2026-07-25 08:59:00', '2026-07-25 08:58:00', 'ORDER_20260725_0002', 'pay_key_0002', 'DONE', 5);

INSERT INTO review (id, member_id, room_id, rating, content) VALUES
(1, 2, 1, 5, '조용하고 깨끗해서 집중 잘 됐어요.'),
(2, 5, 5, 4, '넓고 좋은데 가격이 조금 아쉬워요.');

INSERT INTO wishlist (id, member_id, room_id) VALUES
(1, 3, 4),
(2, 4, 5),
(3, 2, 2);
