import { createHashRouter, redirect } from 'react-router'
import LoginPage from './pages/auth/LoginPage'
import SignupPage from './pages/auth/SignupPage'
import FindIdPage from './pages/auth/FindIdPage'
import FindPasswordPage from './pages/auth/FindPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'
import AdminLayout from './pages/admin/AdminLayout'
import UserLayout from './pages/user/UserLayout'
import AdminRoomsPage from './pages/admin/AdminRoomsPage'
import RoomReviewsPage from './pages/admin/RoomReviewsPage'
import MyPage from './pages/admin/MyPage'
import DashboardPage from './pages/admin/DashboardPage'
import ReservationManagePage from './pages/admin/ReservationManagePage'
import ReservePage from './pages/user/ReservePage'
import PaymentPage from './pages/user/PaymentPage'
import RoomDetailPage from './pages/user/RoomDetailPage'
import WishlistPage from './pages/user/WishlistPage'
import ReservationHistoryPage from './pages/user/ReservationHistoryPage'

export const router = createHashRouter([
  {
    path: '/',
    loader: () => redirect('/login'),
  },
  {
    path: '/login',
    Component: LoginPage,
  },
  {
    path: '/signup',
    Component: SignupPage,
  },
  {
    path: '/find-id',
    Component: FindIdPage,
  },
  {
    path: '/find-password',
    Component: FindPasswordPage,
  },
  {
    path: '/reset-password',
    Component: ResetPasswordPage,
  },
  // ── 일반 사용자 도메인 ──────────────────────────────
  {
    path: '/user',
    Component: UserLayout,
    children: [
      { index: true, loader: () => redirect('/user/rooms') },
      { path: 'rooms', Component: AdminRoomsPage },
      { path: 'rooms/:roomId', Component: RoomDetailPage },
      { path: 'rooms/:roomId/reviews', Component: RoomReviewsPage },
      { path: 'rooms/:roomId/reserve', Component: ReservePage },
      { path: 'rooms/:roomId/payment', Component: PaymentPage },
      { path: 'wishlist', Component: WishlistPage },
      { path: 'reservations', Component: ReservationHistoryPage },
      { path: 'mypage', Component: MyPage },
    ],
  },
  // ── 관리자 도메인 ──────────────────────────────────
  {
    path: '/admin',
    Component: AdminLayout,
    children: [
      { index: true, loader: () => redirect('/admin/dashboard') },
      { path: 'dashboard', Component: DashboardPage },
      { path: 'rooms', Component: AdminRoomsPage },
      { path: 'rooms/:roomId/reviews', Component: RoomReviewsPage },
      { path: 'reservations', Component: ReservationManagePage },
    ],
  },
])
