import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import { useAuth, GRADE_CONFIG } from '../../context/AuthContext'

/* 취소특가 카운트다운: 방 id를 시드로 삼아 15분 주기 내 위상을 다르게 */
export function CancelSaleCountdown({ roomId }: { roomId: number }) {
  const TOTAL = 15 * 60 // 15분
  const getRemaining = () => {
    const offset = (roomId * 97) % TOTAL // 방마다 시작 위상 차이
    const elapsed = (Math.floor(Date.now() / 1000) + offset) % TOTAL
    return TOTAL - elapsed
  }
  const [secs, setSecs] = useState(getRemaining)
  useEffect(() => {
    const id = setInterval(() => setSecs(getRemaining()), 1000)
    return () => clearInterval(id)
  }, [])
  const m = String(Math.floor(secs / 60)).padStart(2, '0')
  const s = String(secs % 60).padStart(2, '0')
  const urgent = secs < 60
  return (
    <div className="flex items-center gap-1 px-2 py-1 rounded-lg"
      style={{ background: urgent ? '#fef2f2' : '#fff7ed', border: `1px solid ${urgent ? '#fca5a5' : '#fed7aa'}` }}>
      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke={urgent ? '#dc2626' : '#ea580c'} strokeWidth="2.5" strokeLinecap="round">
        <circle cx="12" cy="12" r="10" /><path d="M12 6v6l3 3" />
      </svg>
      <span className="text-[11px] font-bold tabular-nums leading-none" style={{ color: urgent ? '#dc2626' : '#ea580c' }}>
        {m}:{s}
      </span>
    </div>
  )
}

export type Room = {
  id: number
  name: string
  capacity: number
  price: number
  cancelSalePrice: number | null
  status: 'available' | 'reserved'
  reservedSlots: number[]
  baseFavCount: number
}

export const ROOMS: Room[] = [
  { id: 1, name: '세미나실 A',    capacity: 8,  price: 5000, cancelSalePrice: 3500, status: 'available', reservedSlots: [10, 11, 14],              baseFavCount: 12 },
  { id: 2, name: '소회의실 B',    capacity: 4,  price: 3000, cancelSalePrice: null, status: 'reserved',  reservedSlots: [9,10,11,12,13,14,15,16,17,18], baseFavCount: 5  },
  { id: 3, name: '독서실 C',      capacity: 1,  price: 1500, cancelSalePrice: 900,  status: 'available', reservedSlots: [9, 13, 14],               baseFavCount: 8  },
  { id: 4, name: '그룹스터디룸 D', capacity: 6,  price: 4500, cancelSalePrice: null, status: 'available', reservedSlots: [],                         baseFavCount: 20 },
  { id: 5, name: '세미나실 E',    capacity: 10, price: 6000, cancelSalePrice: 4200, status: 'available', reservedSlots: [16, 17, 18],               baseFavCount: 17 },
  { id: 6, name: '소회의실 F',    capacity: 4,  price: 3000, cancelSalePrice: null, status: 'available', reservedSlots: [9, 10],                    baseFavCount: 3  },
  { id: 7, name: '독서실 G',      capacity: 1,  price: 1500, cancelSalePrice: null, status: 'available', reservedSlots: [15, 16],                   baseFavCount: 6  },
]

const HOURS = Array.from({ length: 12 }, (_, i) => i + 9)

const STATUS_LABEL = {
  available: { label: '사용 가능', color: '#16a34a', bg: '#f0fdf4', dot: '#22c55e' },
  reserved:  { label: '예약됨',   color: '#b45309', bg: '#fffbeb', dot: '#f59e0b' },
}

/* ─── 서브 컴포넌트 ──────────────────────────────────── */

function StatusBadge({ status }: { status: Room['status'] }) {
  const s = STATUS_LABEL[status]
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" style={{ color: s.color, background: s.bg }}>
      <span className="w-1.5 h-1.5 rounded-full" style={{ background: s.dot }} />
      {s.label}
    </span>
  )
}

export function TimeSlotRow({ room, currentHour }: { room: Room; currentHour: number | null }) {
  return (
    <div>
      <div className="flex mb-0.5">
        {HOURS.map((h) => (
          <div key={h} className="flex-1 text-center" style={{ minWidth: 0 }}>
            <span className="text-[9px] font-medium" style={{ color: h === currentHour ? '#1e3a5f' : '#94a3b8' }}>{h}</span>
          </div>
        ))}
      </div>
      <div className="flex gap-px rounded-md overflow-hidden" style={{ height: '18px' }}>
        {HOURS.map((h) => {
          const isRes = room.reservedSlots.includes(h)
          const isCur = h === currentHour
          const bg = isRes ? '#fde68a' : '#bbf7d0'
          return (
            <div key={h} title={`${h}:00  ${isRes ? '예약됨' : '사용 가능'}${isCur ? '  ← 현재' : ''}`}
              className="flex-1 relative flex items-center justify-center" style={{ background: bg, minWidth: 0 }}>
              {isCur && (
                <svg width="7" height="7" viewBox="0 0 6 6" className="absolute pointer-events-none">
                  <polygon points="3,0 6,3 3,6 0,3" fill="#1e3a5f" />
                </svg>
              )}
            </div>
          )
        })}
      </div>
      <div className="flex items-center gap-3 mt-1">
        {[
          { bg: '#bbf7d0', l: '가능', symbol: false },
          { bg: '#fde68a', l: '예약됨', symbol: false },
          ...(currentHour !== null ? [{ bg: '', l: '현재', symbol: true }] : []),
        ].map((x) => (
          <div key={x.l} className="flex items-center gap-1">
            {x.symbol ? (
              <svg width="8" height="8" viewBox="0 0 6 6"><polygon points="3,0 6,3 3,6 0,3" fill="#1e3a5f" /></svg>
            ) : (
              <div className="w-2 h-2 rounded-sm" style={{ background: x.bg }} />
            )}
            <span className="text-[9px] text-gray-400">{x.l}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

function StarRating({ value }: { value: number }) {
  return (
    <div className="flex gap-0.5">
      {[1,2,3,4,5].map((i) => (
        <svg key={i} width="11" height="11" viewBox="0 0 24 24" fill={i <= value ? '#f59e0b' : '#e5e7eb'}>
          <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
        </svg>
      ))}
    </div>
  )
}

/* ─── 방 생성 다이얼로그 ─────────────────────────────── */

function CreateRoomDialog({ onClose, onCreate }: { onClose: () => void; onCreate: (room: Room) => void }) {
  const [name, setName]         = useState('')
  const [capacity, setCapacity] = useState('')
  const [price, setPrice]       = useState('')
  const [submitting, setSubmitting] = useState(false)

  const canSubmit = name.trim() && Number(capacity) > 0 && Number(price) > 0

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!canSubmit) return
    setSubmitting(true)
    setTimeout(() => {
      const newRoom: Room = {
        id: Date.now(),
        name: name.trim(),
        capacity: Number(capacity),
        price: Number(price),
        cancelSalePrice: null,
        status: 'available',
        reservedSlots: [],
        baseFavCount: 0,
      }
      onCreate(newRoom)
    }, 400)
  }

  const fields = [
    { label: '방 이름', value: name, setter: setName, placeholder: '예) 세미나실 H', type: 'text' },
    { label: '수용 인원 (명)', value: capacity, setter: setCapacity, placeholder: '예) 6', type: 'number' },
    { label: '시간당 가격 (원)', value: price, setter: setPrice, placeholder: '예) 4000', type: 'number' },
  ] as const

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4" style={{ background: 'rgba(0,0,0,0.45)' }}
      onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="w-full max-w-sm rounded-2xl overflow-hidden" style={{ background: '#fff', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}>
        <div className="px-6 py-5" style={{ borderBottom: '1px solid #f1f5f9' }}>
          <h2 className="text-base font-bold text-gray-900">새 방 추가</h2>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-5 flex flex-col gap-4">
          {fields.map(({ label, value, setter, placeholder, type }) => (
            <div key={label}>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
              <input
                type={type}
                value={value}
                onChange={(e) => (setter as (v: string) => void)(e.target.value)}
                placeholder={placeholder}
                min={type === 'number' ? 1 : undefined}
                className="w-full px-4 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all"
                style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }}
              />
            </div>
          ))}
        </form>
        <div className="flex gap-2 px-6 py-4" style={{ borderTop: '1px solid #f1f5f9' }}>
          <button onClick={onClose} className="flex-1 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-100 transition-all">
            취소
          </button>
          <button
            onClick={handleSubmit as unknown as React.MouseEventHandler}
            disabled={!canSubmit || submitting}
            className="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white transition-all hover:opacity-90 disabled:opacity-40"
            style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}
          >
            {submitting ? '추가 중...' : '추가'}
          </button>
        </div>
      </div>
    </div>
  )
}

/* ─── 수정 다이얼로그 ─────────────────────────────────── */

type EditForm = { name: string; capacity: string; price: string; status: Room['status'] }

function EditRoomDialog({ room, onClose }: { room: Room; onClose: () => void }) {
  const [form, setForm] = useState<EditForm>({
    name: room.name, capacity: String(room.capacity), price: String(room.price),
    status: room.status,
  })
  const set = (k: keyof EditForm) => (v: string) => setForm((f) => ({ ...f, [k]: v }))

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: 'rgba(0,0,0,0.4)' }}>
      <div className="w-full max-w-md rounded-2xl shadow-2xl" style={{ background: '#fff' }}>
        <div className="flex items-center justify-between px-6 py-4" style={{ borderBottom: '1px solid #f1f5f9' }}>
          <h3 className="text-base font-bold text-gray-900">스터디룸 수정</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <div className="px-6 py-5 flex flex-col gap-4">
          {[
            { label: '방 이름', key: 'name' as const, type: 'text', placeholder: '세미나실 A' },
            { label: '수용 인원 (명)', key: 'capacity' as const, type: 'number', placeholder: '8' },
            { label: '시간당 요금 (원)', key: 'price' as const, type: 'number', placeholder: '5000' },
          ].map(({ label, key, type, placeholder }) => (
            <div key={key}>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">{label}</label>
              <input type={type} value={form[key]} placeholder={placeholder}
                onChange={(e) => set(key)(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl text-sm text-gray-900 outline-none transition-all"
                style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }} />
            </div>
          ))}

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">상태</label>
            <div className="flex gap-2">
              {(['available', 'reserved'] as const).map((s) => (
                <button key={s} type="button" onClick={() => set('status')(s)}
                  className="flex-1 py-2 rounded-xl text-xs font-semibold border-[1.5px] transition-all"
                  style={{
                    background: form.status === s ? (s === 'available' ? '#f0fdf4' : '#fffbeb') : '#f8fafc',
                    color: form.status === s ? (s === 'available' ? '#16a34a' : '#b45309') : '#94a3b8',
                    borderColor: form.status === s ? (s === 'available' ? '#86efac' : '#fde68a') : '#e2e8f0',
                  }}>
                  {s === 'available' ? '사용 가능' : '예약됨'}
                </button>
              ))}
            </div>
          </div>

        </div>

        <div className="flex gap-2 px-6 py-4" style={{ borderTop: '1px solid #f1f5f9' }}>
          <button onClick={onClose} className="flex-1 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-100 transition-all">
            취소
          </button>
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white transition-all hover:opacity-90"
            style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
            저장
          </button>
        </div>
      </div>
    </div>
  )
}

/* ─── 갤러리 카드 ─────────────────────────────────── */

function RoomCard({
  room, isFav, isMyRoom, isUsingNow, favCount, dp, discountRate, avg, reviewCount, canReview, isAdmin, onFav, onReview, onEdit, onDelete, onReserve,
}: {
  room: Room; isFav: boolean; isMyRoom: boolean; isUsingNow: boolean; favCount: number; dp: number | null; discountRate: number
  avg: number | null; reviewCount: number; canReview: boolean; isAdmin: boolean
  onFav: () => void; onReview: () => void; onEdit: () => void; onDelete: () => void; onReserve: () => void
}) {
  return (
    <div className="rounded-2xl overflow-hidden flex flex-col transition-shadow hover:shadow-md"
      style={{ background: isMyRoom ? '#f0f9ff' : '#fff', border: `1.5px solid ${isMyRoom ? '#bfdbfe' : '#e8edf5'}` }}>

      {/* 헤더 */}
      <div className="px-5 pt-5 pb-3 flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl flex items-center justify-center text-sm font-bold"
            style={{ background: isMyRoom ? '#dbeafe' : '#eff6ff', color: '#2d5a9e' }}>{room.id}</div>
          <div>
            <div className="flex items-center gap-1.5 flex-wrap">
              <button onClick={onReview} className="text-sm font-bold text-gray-900 hover:text-blue-700 transition-colors">{room.name}</button>
              {isMyRoom && <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold" style={{ background: '#dbeafe', color: '#1d4ed8' }}>예약함</span>}
              {room.cancelSalePrice && <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold" style={{ background: '#fef2f2', color: '#dc2626' }}>취소특가</span>}
            </div>
            {room.cancelSalePrice && <CancelSaleCountdown roomId={room.id} />}
            <div className="text-xs font-medium mt-0.5" style={{ color: '#4b6a8a' }}>최대 {room.capacity}명</div>
          </div>
        </div>
        <div className="flex flex-col items-center gap-0.5 shrink-0">
          <button onClick={onFav} className="p-1.5 rounded-lg transition-all hover:scale-110">
            <svg width="16" height="16" viewBox="0 0 24 24" fill={isFav ? '#f59e0b' : 'none'} stroke={isFav ? '#f59e0b' : '#cbd5e1'} strokeWidth="2" strokeLinecap="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
          </button>
          <span className="text-[10px] font-semibold leading-none tabular-nums" style={{ color: isFav ? '#b45309' : '#94a3b8' }}>{favCount}</span>
        </div>
      </div>

      {/* 가격 + 평점 */}
      <div className="px-5 pb-3 flex items-center justify-between">
        <div>
          {dp ? (
            <div>
              <span className="text-base font-bold text-gray-900">{dp.toLocaleString()}원</span>
              <span className="ml-1.5 text-xs text-gray-400 line-through">{room.price.toLocaleString()}원</span>
              <span className="ml-1 text-[10px] font-bold px-1 py-0.5 rounded" style={{ background: '#fee2e2', color: '#dc2626' }}>-{Math.round(discountRate * 100)}%</span>
            </div>
          ) : room.cancelSalePrice ? (
            <div>
              <span className="text-base font-bold" style={{ color: '#dc2626' }}>{room.cancelSalePrice.toLocaleString()}원</span>
              <span className="ml-1.5 text-xs text-gray-400 line-through">{room.price.toLocaleString()}원</span>
            </div>
          ) : (
            <span className="text-base font-semibold text-gray-900">{room.price.toLocaleString()}원</span>
          )}
        </div>
        {avg !== null ? (
          <div className="text-right">
            <StarRating value={Math.round(avg)} />
            <div className="text-[10px] text-gray-400 mt-0.5">{avg.toFixed(1)} ({reviewCount}개)</div>
          </div>
        ) : <span className="text-xs text-gray-300">리뷰 없음</span>}
      </div>

      {/* 상태 */}
      <div className="px-5 pb-3 flex items-center justify-between">
        {isUsingNow ? (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" style={{ color: '#1e40af', background: '#dbeafe' }}>
            <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: '#3b82f6' }} />
            사용 중
          </span>
        ) : (
          <StatusBadge status={room.status} />
        )}
      </div>

      {/* 하단 버튼 */}
      <div className="px-5 pb-4 mt-auto flex gap-2">
        {isAdmin ? (
          <>
            <button onClick={onEdit} className="flex-1 py-2 rounded-xl text-xs font-semibold transition-all hover:opacity-90"
              style={{ background: '#eff6ff', color: '#2d5a9e', border: '1px solid #bfdbfe' }}>수정</button>
            <button onClick={onDelete} className="flex-1 py-2 rounded-xl text-xs font-semibold transition-all hover:opacity-90"
              style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca' }}>삭제</button>
          </>
        ) : (
          <>
            <button onClick={onReserve} disabled={room.status === 'reserved'}
              className="flex-1 py-2 rounded-xl text-xs font-semibold text-white transition-all hover:opacity-90 disabled:opacity-40"
              style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>예약하기</button>
            {canReview && (
              <button onClick={onReview} className="py-2 px-3 rounded-xl text-xs font-semibold transition-all hover:opacity-90"
                style={{ background: '#f8fafc', color: '#64748b', border: '1px solid #e2e8f0' }}>리뷰</button>
            )}
          </>
        )}
      </div>
    </div>
  )
}

/* ─── 메인 페이지 ─────────────────────────────────── */

export default function AdminRoomsPage() {
  const { isAdmin, grade, favorites, toggleFavorite, reservations, reviews } = useAuth()
  const navigate = useNavigate()
  const [rooms, setRooms] = useState<Room[]>(ROOMS)
  const [showCreate, setShowCreate] = useState(false)

  const [searchName, setSearchName]     = useState('')
  const [maxPrice, setMaxPrice]         = useState('')
  const [minCapacity, setMinCapacity]   = useState('')
  const [filterStatus, setFilterStatus] = useState<Room['status'] | 'all'>('all')
  const [tagCancelSale, setTagCancelSale] = useState(false)
  const [tagFavorite, setTagFavorite]     = useState(false)
  const [viewMode, setViewMode]           = useState<'list' | 'gallery'>('list')
  const [editRoom, setEditRoom]           = useState<Room | null>(null)

  const now = new Date()
  const currentHour = now.getHours()
  const displayHour = currentHour >= 9 && currentHour <= 20 ? currentHour : null

  const discountRate = (!isAdmin && grade) ? GRADE_CONFIG[grade].discount : 0
  const discountedPrice = (price: number) => discountRate > 0 ? Math.round(price * (1 - discountRate)) : null

  const filtered = rooms.filter((r) => {
    if (searchName && !r.name.includes(searchName)) return false
    if (tagCancelSale && !r.cancelSalePrice) return false
    if (tagFavorite && !favorites.includes(r.id)) return false
    const effectivePrice = discountedPrice(r.price) ?? r.cancelSalePrice ?? r.price
    if (maxPrice && effectivePrice > Number(maxPrice)) return false
    if (minCapacity && r.capacity < Number(minCapacity)) return false
    if (filterStatus !== 'all' && r.status !== filterStatus) return false
    return true
  })

  const nowAvailable = displayHour !== null
    ? rooms.filter((r) => r.status === 'available' && !r.reservedSlots.includes(displayHour)).length
    : rooms.filter((r) => r.status === 'available').length
  const nowReserved = displayHour !== null
    ? rooms.filter((r) => r.reservedSlots.includes(displayHour)).length
    : rooms.filter((r) => r.status === 'reserved').length

  const hasReservation  = (roomId: number) => reservations.some((r) => r.roomId === roomId && r.status === 'confirmed')
  const roomReviews     = (roomId: number) => reviews.filter((r) => r.roomId === roomId)
  const avgRating       = (roomId: number) => {
    const rs = roomReviews(roomId); return rs.length ? rs.reduce((a, b) => a + b.rating, 0) / rs.length : null
  }

  const isUsingNow = (room: Room) =>
    !isAdmin && displayHour !== null &&
    room.reservedSlots.includes(displayHour) &&
    reservations.some((r) => r.roomId === room.id && r.status === 'confirmed' && r.startHour <= displayHour && r.endHour > displayHour)

  const favCount = (room: Room) => room.baseFavCount + (favorites.includes(room.id) ? 1 : 0)

  const rowProps = (room: Room) => ({
    room,
    isFav:        favorites.includes(room.id),
    isMyRoom:     !isAdmin && hasReservation(room.id),
    isUsingNow:   isUsingNow(room),
    favCount:     favCount(room),
    dp:           discountedPrice(room.price),
    discountRate,
    avg:          avgRating(room.id),
    reviewCount:  roomReviews(room.id).length,
    canReview:    !isAdmin && hasReservation(room.id),
    isAdmin,
    onFav:        () => toggleFavorite(room.id),
    onReview:     () => navigate(`${isAdmin ? '/admin' : '/user'}/rooms/${room.id}/reviews`),
    onEdit:       () => setEditRoom(room),
    onDelete:     () => {},
    onReserve:    () => navigate(`/user/rooms/${room.id}/reserve`),
  })

  return (
    <div className="flex-1 overflow-auto">
      {showCreate && (
        <CreateRoomDialog
          onClose={() => setShowCreate(false)}
          onCreate={(room) => { setRooms((prev) => [...prev, room]); setShowCreate(false) }}
        />
      )}
      {editRoom && <EditRoomDialog room={editRoom} onClose={() => setEditRoom(null)} />}

      <div className="max-w-6xl mx-auto px-6 py-8">

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900" style={{ letterSpacing: '-0.02em' }}>스터디룸 목록</h1>
            <p className="text-sm text-gray-500 mt-1">전체 {rooms.length}개의 방</p>
          </div>
          <div className="flex items-center gap-2">
            {/* 갤러리/리스트 토글 */}
            <div className="flex rounded-xl overflow-hidden" style={{ border: '1.5px solid #e2e8f0' }}>
              {(['list', 'gallery'] as const).map((m) => (
                <button key={m} onClick={() => setViewMode(m)}
                  className="px-3 py-2 transition-all"
                  style={{ background: viewMode === m ? '#1e3a5f' : '#fff', color: viewMode === m ? '#fff' : '#94a3b8' }}>
                  {m === 'list' ? (
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                      <line x1="8" y1="6" x2="21" y2="6" /><line x1="8" y1="12" x2="21" y2="12" /><line x1="8" y1="18" x2="21" y2="18" />
                      <line x1="3" y1="6" x2="3.01" y2="6" /><line x1="3" y1="12" x2="3.01" y2="12" /><line x1="3" y1="18" x2="3.01" y2="18" />
                    </svg>
                  ) : (
                    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                      <rect x="3" y="3" width="7" height="7" /><rect x="14" y="3" width="7" height="7" />
                      <rect x="3" y="14" width="7" height="7" /><rect x="14" y="14" width="7" height="7" />
                    </svg>
                  )}
                </button>
              ))}
            </div>
            {isAdmin && (
              <button onClick={() => setShowCreate(true)} className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-semibold text-white transition-all hover:opacity-90"
                style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)', boxShadow: '0 4px 12px rgba(30,58,95,0.3)' }}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
                  <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
                </svg>
                방 추가
              </button>
            )}
          </div>
        </div>

        {/* 할인 배너 */}
        {!isAdmin && grade && discountRate > 0 && (
          <div className="flex items-center gap-3 px-4 py-3 rounded-xl mb-5 text-sm font-medium"
            style={{ background: GRADE_CONFIG[grade].bg, color: GRADE_CONFIG[grade].color, border: `1px solid ${GRADE_CONFIG[grade].color}22` }}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
            </svg>
            <span><b>{GRADE_CONFIG[grade].label}</b> 등급 혜택: 모든 방 <b>{Math.round(discountRate * 100)}% 할인</b> 적용 중</span>
          </div>
        )}

        {/* 현재 시간 + 통계 */}
        <div className="mb-3 flex items-center gap-2">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2" strokeLinecap="round"><circle cx="12" cy="12" r="10" /><path d="M12 6v6l4 2" /></svg>
          <span className="text-xs font-medium text-gray-500">
            현재 시간: <span className="font-bold text-gray-800">{displayHour !== null ? `${String(displayHour).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}` : '운영 시간 외'}</span>
            {displayHour !== null && <span className="ml-1 text-gray-400">({displayHour}:00–{displayHour + 1}:00 기준)</span>}
          </span>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
          {[
            { label: '전체', value: rooms.length, color: '#2d5a9e', bg: '#eff6ff', sub: null },
            { label: '사용 가능', value: nowAvailable, color: '#16a34a', bg: '#f0fdf4', sub: displayHour ? '현재 기준' : null },
            { label: '예약됨', value: nowReserved, color: '#b45309', bg: '#fffbeb', sub: displayHour ? '현재 기준' : null },
          ].map((s) => (
            <div key={s.label} className="rounded-2xl p-5" style={{ background: '#ffffff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
              <div className="flex items-center justify-between mb-1">
                <div className="text-xs font-medium text-gray-500">{s.label}</div>
                {s.sub && <span className="text-[10px] font-medium px-1.5 py-0.5 rounded-md" style={{ background: '#f1f5f9', color: '#64748b' }}>{s.sub}</span>}
              </div>
              <div className="text-3xl font-bold" style={{ color: s.color, letterSpacing: '-0.03em' }}>{s.value}</div>
            </div>
          ))}
        </div>

        {/* 검색·필터 */}
        <div className="rounded-2xl p-4 mb-5 flex flex-col gap-3" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
          <div className="flex flex-wrap gap-3">
            <div className="relative flex-1 min-w-[160px]">
              <div className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="8" /><path d="M21 21l-4.35-4.35" /></svg>
              </div>
              <input type="text" placeholder="방 이름 검색" value={searchName} onChange={(e) => setSearchName(e.target.value)}
                className="w-full pl-9 pr-3 py-2.5 rounded-xl text-sm text-gray-900 placeholder-gray-400 outline-none"
                style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }} />
            </div>
            <div className="relative min-w-[150px]">
              <div className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400 text-xs font-medium">₩</div>
              <input type="number" placeholder="최대 가격 (원)" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)}
                className="w-full pl-7 pr-3 py-2.5 rounded-xl text-sm placeholder-gray-400 outline-none"
                style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }} />
            </div>
            <div className="relative min-w-[150px]">
              <div className="absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75" /></svg>
              </div>
              <input type="number" placeholder="최소 인원 (명)" value={minCapacity} onChange={(e) => setMinCapacity(e.target.value)}
                className="w-full pl-9 pr-3 py-2.5 rounded-xl text-sm placeholder-gray-400 outline-none"
                style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }} />
            </div>
          </div>

          {/* 태그 + 상태 필터 */}
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xs text-gray-400 mr-1">필터</span>
            {/* 취소특가 태그 */}
            <button onClick={() => setTagCancelSale((v) => !v)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all"
              style={{ background: tagCancelSale ? '#fef2f2' : '#f8fafc', color: tagCancelSale ? '#dc2626' : '#64748b', border: `1.5px solid ${tagCancelSale ? '#fca5a5' : '#e2e8f0'}` }}>
              <svg width="11" height="11" viewBox="0 0 24 24" fill={tagCancelSale ? '#dc2626' : 'none'} stroke={tagCancelSale ? '#dc2626' : 'currentColor'} strokeWidth="2" strokeLinecap="round">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
              취소 특가
            </button>
            {/* 즐겨찾기 태그 */}
            <button onClick={() => setTagFavorite((v) => !v)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-all"
              style={{ background: tagFavorite ? '#fffbeb' : '#f8fafc', color: tagFavorite ? '#b45309' : '#64748b', border: `1.5px solid ${tagFavorite ? '#fde68a' : '#e2e8f0'}` }}>
              <svg width="11" height="11" viewBox="0 0 24 24" fill={tagFavorite ? '#f59e0b' : 'none'} stroke={tagFavorite ? '#f59e0b' : 'currentColor'} strokeWidth="2" strokeLinecap="round">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
              </svg>
              즐겨찾기
            </button>
            <div className="w-px h-4 bg-gray-200 mx-1" />
            {(['all', 'available', 'reserved'] as const).map((s) => {
              const labels = { all: '전체', available: '사용 가능', reserved: '예약됨' }
              const active = filterStatus === s
              return (
                <button key={s} onClick={() => setFilterStatus(s)}
                  className="px-3.5 py-1.5 rounded-full text-xs font-semibold transition-all whitespace-nowrap"
                  style={{ background: active ? '#1e3a5f' : '#f8fafc', color: active ? '#fff' : '#64748b', border: `1.5px solid ${active ? '#1e3a5f' : '#e2e8f0'}` }}>
                  {labels[s]}
                </button>
              )
            })}
          </div>
        </div>

        {/* ─── 갤러리 뷰 ─── */}
        {viewMode === 'gallery' ? (
          filtered.length === 0
            ? <div className="text-center py-16 text-sm text-gray-400">검색 결과가 없습니다</div>
            : <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">{filtered.map((room) => <RoomCard key={room.id} {...rowProps(room)} />)}</div>
        ) : (
        /* ─── 리스트 뷰 ─── */
        <div className="rounded-2xl overflow-hidden" style={{ border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
          {/* 모바일: 카드 스택 */}
          <div className="md:hidden">
            {filtered.length === 0
              ? <div className="text-center py-16 text-sm text-gray-400">검색 결과가 없습니다</div>
              : <div className="p-3 flex flex-col gap-3">{filtered.map((room) => <RoomCard key={room.id} {...rowProps(room)} />)}</div>
            }
          </div>
          {/* 데스크톱: 테이블 */}
          <div className="hidden md:block overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e8edf5' }}>
                  {['', '방 이름', '가격', '평점', '시간대 예약 현황', '상태', ...(isAdmin ? ['관리'] : ['예약 / 리뷰'])].map((h, i) => (
                    <th key={i} className="text-left px-4 py-3.5 text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.length === 0 ? (
                  <tr><td colSpan={7} className="text-center py-16 text-sm text-gray-400">검색 결과가 없습니다</td></tr>
                ) : filtered.map((room, i) => {
                  const p  = rowProps(room)
                  const dp = p.dp
                  return (
                    <tr key={room.id} className="transition-colors"
                      style={{ borderBottom: i < filtered.length - 1 ? '1px solid #f1f5f9' : 'none', background: p.isMyRoom ? '#f0f9ff' : '#fff' }}>
                      {/* 즐겨찾기 */}
                      <td className="pl-4 pr-1 py-4">
                        <div className="flex flex-col items-center gap-0.5">
                          <button onClick={p.onFav} className="p-1.5 rounded-lg transition-all hover:scale-110">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill={p.isFav ? '#f59e0b' : 'none'} stroke={p.isFav ? '#f59e0b' : '#cbd5e1'} strokeWidth="2" strokeLinecap="round">
                              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                            </svg>
                          </button>
                          <span className="text-[10px] font-semibold tabular-nums leading-none" style={{ color: p.isFav ? '#b45309' : '#94a3b8' }}>{p.favCount}</span>
                        </div>
                      </td>
                      {/* 방 이름 */}
                      <td className="px-3 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0 text-sm font-bold"
                            style={{ background: p.isMyRoom ? '#dbeafe' : '#eff6ff', color: '#2d5a9e' }}>{room.id}</div>
                          <div>
                            <div className="flex items-center gap-1.5 flex-wrap">
                              <button onClick={p.onReview} className="text-sm font-semibold text-gray-900 hover:text-blue-700 transition-colors">{room.name}</button>
                              {p.isMyRoom && <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold" style={{ background: '#dbeafe', color: '#1d4ed8' }}>예약함</span>}
                              {room.cancelSalePrice && <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold" style={{ background: '#fef2f2', color: '#dc2626' }}>취소특가</span>}
                            </div>
                            <div className="flex items-center gap-2 mt-0.5">
                              <div className="text-xs font-medium" style={{ color: '#4b6a8a' }}>최대 {room.capacity}명</div>
                              {room.cancelSalePrice && <CancelSaleCountdown roomId={room.id} />}
                            </div>
                          </div>
                        </div>
                      </td>
                      {/* 가격 */}
                      <td className="px-4 py-4 whitespace-nowrap">
                        {dp ? (
                          <div>
                            <div className="text-sm font-bold text-gray-900">{dp.toLocaleString()}원</div>
                            <div className="flex items-center gap-1 mt-0.5">
                              <span className="text-xs text-gray-400 line-through">{room.price.toLocaleString()}원</span>
                              <span className="text-[10px] font-bold px-1 py-0.5 rounded" style={{ background: '#fee2e2', color: '#dc2626' }}>-{Math.round(discountRate * 100)}%</span>
                            </div>
                          </div>
                        ) : room.cancelSalePrice ? (
                          <div>
                            <div className="text-sm font-bold" style={{ color: '#dc2626' }}>{room.cancelSalePrice.toLocaleString()}원</div>
                            <div className="text-xs text-gray-400 line-through mt-0.5">{room.price.toLocaleString()}원</div>
                          </div>
                        ) : (
                          <div className="text-sm font-semibold text-gray-900">{room.price.toLocaleString()}원</div>
                        )}
                      </td>
                      {/* 평점 */}
                      <td className="px-4 py-4 whitespace-nowrap">
                        {p.avg !== null ? (
                          <div><StarRating value={Math.round(p.avg)} /><div className="text-xs text-gray-400 mt-0.5">{p.avg.toFixed(1)} ({p.reviewCount}개)</div></div>
                        ) : <span className="text-xs text-gray-300">없음</span>}
                      </td>
                      {/* 시간대 */}
                      <td className="px-4 py-4" style={{ minWidth: '260px' }}>
                        <TimeSlotRow room={room} currentHour={displayHour} />
                      </td>
                      {/* 상태 */}
                      <td className="px-4 py-4">
                        {p.isUsingNow ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" style={{ color: '#1e40af', background: '#dbeafe' }}>
                            <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: '#3b82f6' }} />
                            사용 중
                          </span>
                        ) : (
                          <StatusBadge status={room.status} />
                        )}
                      </td>
                      {/* 관리/리뷰 */}
                      {isAdmin ? (
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-1">
                            <button onClick={p.onEdit} className="p-2 rounded-lg text-gray-400 hover:text-blue-700 hover:bg-blue-50 transition-all" title="수정">
                              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                                <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z" />
                              </svg>
                            </button>
                            <button onClick={p.onDelete} className="p-2 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 transition-all" title="삭제">
                              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
                                <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" /><path d="M10 11v6M14 11v6M9 6V4a1 1 0 011-1h4a1 1 0 011 1v2" />
                              </svg>
                            </button>
                          </div>
                        </td>
                      ) : (
                        <td className="px-4 py-4">
                          <div className="flex items-center gap-1.5">
                            <button onClick={p.onReserve} disabled={room.status === 'reserved'}
                              className="px-3 py-1.5 rounded-lg text-xs font-semibold text-white transition-all hover:opacity-90 disabled:opacity-40 whitespace-nowrap"
                              style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
                              예약
                            </button>
                            {p.canReview
                              ? <button onClick={p.onReview} className="px-3 py-1.5 rounded-lg text-xs font-semibold transition-all hover:opacity-90 whitespace-nowrap" style={{ background: '#eff6ff', color: '#2d5a9e', border: '1px solid #bfdbfe' }}>리뷰</button>
                              : <span className="text-xs text-gray-300 whitespace-nowrap">리뷰 불가</span>}
                          </div>
                        </td>
                      )}
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
          <div className="px-5 py-3 flex items-center justify-between" style={{ background: '#f8fafc', borderTop: '1px solid #e8edf5' }}>
            <span className="text-xs text-gray-400">{filtered.length}개 표시 중 (전체 {rooms.length}개)</span>
            <div className="flex items-center gap-1">
              <button className="p-1.5 rounded-lg text-gray-400 disabled:opacity-30" disabled>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><polyline points="15 18 9 12 15 6" /></svg>
              </button>
              <span className="px-3 py-1 rounded-lg text-xs font-semibold text-white" style={{ background: '#1e3a5f' }}>1</span>
              <button className="p-1.5 rounded-lg text-gray-400 disabled:opacity-30" disabled>
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><polyline points="9 18 15 12 9 6" /></svg>
              </button>
            </div>
          </div>
        </div>
        )}
      </div>
    </div>
  )
}
