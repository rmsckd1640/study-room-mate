import { createContext, useContext, useState, type ReactNode } from 'react'

type Role = 'user' | 'admin' | null

export type Grade = 'bronze' | 'silver' | 'gold' | 'platinum'

export const GRADE_CONFIG: Record<Grade, { label: string; discount: number; color: string; bg: string }> = {
  bronze:   { label: '브론즈', discount: 0,    color: '#92400e', bg: '#fef3c7' },
  silver:   { label: '실버',   discount: 0.05, color: '#475569', bg: '#f1f5f9' },
  gold:     { label: '골드',   discount: 0.10, color: '#b45309', bg: '#fffbeb' },
  platinum: { label: 'VIP',    discount: 0.15, color: '#1e40af', bg: '#eff6ff' },
}

export type ReservationStatus = 'pending' | 'confirmed' | 'cancelled' | 'payment_done' | 'rejected'

export type Reservation = {
  id: number
  roomId: number
  roomName: string
  date: string
  startHour: number
  endHour: number
  price: number
  status: ReservationStatus
  userName: string
  cancelReason?: string
}

export type Review = {
  id: number
  roomId: number
  roomName: string
  rating: number
  content: string
  date: string
}

interface AuthContextValue {
  role: Role
  username: string
  name: string
  email: string
  grade: Grade | null
  isAdmin: boolean
  favorites: number[]
  reservations: Reservation[]
  reviews: Review[]
  login: (username: string, role: 'user' | 'admin') => void
  logout: () => void
  toggleFavorite: (roomId: number) => void
  cancelReservation: (id: number, reason?: string) => void
  approveReservation: (id: number) => void
  rejectReservation: (id: number) => void
  addReservation: (r: Omit<Reservation, 'id' | 'userName' | 'status'>) => void
  addReview: (review: Omit<Review, 'id' | 'date'>) => void
  updateReview: (id: number, rating: number, content: string) => void
  deleteReview: (id: number) => void
  updateProfile: (name: string, email: string) => void
  deleteAccount: () => void
}

export const MOCK_RESERVATIONS: Reservation[] = [
  { id: 1, roomId: 1, roomName: '세미나실 A',    date: '2026-07-20', startHour: 10, endHour: 12, price: 8000,  status: 'confirmed',    userName: 'user01' },
  { id: 2, roomId: 3, roomName: '독서실 C',      date: '2026-07-21', startHour: 13, endHour: 14, price: 1200,  status: 'confirmed',    userName: 'user01' },
  { id: 3, roomId: 2, roomName: '소회의실 B',    date: '2026-07-15', startHour: 9,  endHour: 11, price: 6000,  status: 'cancelled',    userName: 'user01' },
  { id: 4, roomId: 5, roomName: '세미나실 E',    date: '2026-07-10', startHour: 16, endHour: 18, price: 10000, status: 'payment_done', userName: 'user01' },
  { id: 5, roomId: 4, roomName: '그룹스터디룸 D', date: '2026-07-23', startHour: 14, endHour: 16, price: 9000,  status: 'pending',      userName: 'user02' },
  { id: 6, roomId: 1, roomName: '세미나실 A',    date: '2026-07-23', startHour: 9,  endHour: 10, price: 4000,  status: 'pending',      userName: 'user03' },
  { id: 7, roomId: 6, roomName: '소회의실 F',    date: '2026-07-22', startHour: 11, endHour: 13, price: 5000,  status: 'rejected',     userName: 'user02' },
]

const MOCK_REVIEWS: Review[] = [
  { id: 1, roomId: 5, roomName: '세미나실 E', rating: 5, content: '조용하고 시설이 정말 좋았어요. 프로젝터 화질도 선명합니다.', date: '2026-07-11' },
]

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [role, setRole]           = useState<Role>(null)
  const [username, setUsername]   = useState('')
  const [name, setName]           = useState('')
  const [email, setEmail]         = useState('')
  const [grade, setGrade]         = useState<Grade | null>(null)
  const [favorites, setFavorites] = useState<number[]>([1, 5])
  const [reservations, setReservations] = useState<Reservation[]>(MOCK_RESERVATIONS)
  const [reviews, setReviews]     = useState<Review[]>(MOCK_REVIEWS)

  const login = (u: string, r: 'user' | 'admin') => {
    setUsername(u); setRole(r)
    setName(r === 'admin' ? '관리자' : '홍길동')
    setEmail(r === 'admin' ? 'admin@studyroom.kr' : 'user01@example.com')
    setGrade(r === 'admin' ? null : 'gold')
  }

  const logout = () => { setRole(null); setUsername(''); setName(''); setEmail(''); setGrade(null) }

  const toggleFavorite = (roomId: number) =>
    setFavorites((f) => f.includes(roomId) ? f.filter((id) => id !== roomId) : [...f, roomId])

  const cancelReservation = (id: number, reason?: string) =>
    setReservations((rs) => rs.map((r) => r.id === id ? { ...r, status: 'cancelled', cancelReason: reason } : r))

  const approveReservation = (id: number) =>
    setReservations((rs) => rs.map((r) => r.id === id ? { ...r, status: 'confirmed' } : r))

  const rejectReservation = (id: number) =>
    setReservations((rs) => rs.map((r) => r.id === id ? { ...r, status: 'rejected' } : r))

  const addReservation = (r: Omit<Reservation, 'id' | 'userName' | 'status'>) =>
    setReservations((rs) => [...rs, { ...r, id: Date.now(), userName: username, status: 'pending' }])

  const addReview = (review: Omit<Review, 'id' | 'date'>) =>
    setReviews((rs) => [...rs, { ...review, id: Date.now(), date: new Date().toISOString().slice(0, 10) }])

  const updateReview = (id: number, rating: number, content: string) =>
    setReviews((rs) => rs.map((r) => r.id === id ? { ...r, rating, content } : r))

  const deleteReview = (id: number) => setReviews((rs) => rs.filter((r) => r.id !== id))

  const updateProfile = (n: string, e: string) => { setName(n); setEmail(e) }

  const deleteAccount = () => {
    setFavorites([]); setReservations([]); setReviews([])
    logout()
  }

  return (
    <AuthContext.Provider value={{ role, username, name, email, grade, isAdmin: role === 'admin', favorites, reservations, reviews, login, logout, toggleFavorite, cancelReservation, approveReservation, rejectReservation, addReservation, addReview, updateReview, deleteReview, updateProfile, deleteAccount }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
