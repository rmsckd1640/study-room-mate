import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router'
import { useAuth, GRADE_CONFIG } from '../../context/AuthContext'
import { ROOMS } from '../admin/AdminRoomsPage'
import { TimeSlotRow, CancelSaleCountdown } from '../admin/AdminRoomsPage'
import { StarRating } from '../../components/rooms/StarRating'
import { Badge } from '../../components/ui/Badge'
import { LoadingSpinner } from '../../components/ui/LoadingSpinner'
import { EmptyState } from '../../components/ui/EmptyState'

export default function RoomDetailPage() {
  const { roomId } = useParams()
  const navigate   = useNavigate()
  const { grade, favorites, toggleFavorite, reviews, reservations, isAdmin } = useAuth()

  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 300)
    return () => clearTimeout(t)
  }, [])

  const id   = Number(roomId)
  const room = ROOMS.find((r) => r.id === id)

  const now         = new Date()
  const currentHour = now.getHours()
  const displayHour = currentHour >= 9 && currentHour <= 20 ? currentHour : null

  const discount      = (!isAdmin && grade) ? GRADE_CONFIG[grade].discount : 0
  const discountedPrice = discount > 0 ? Math.round((room?.price ?? 0) * (1 - discount)) : null

  const isFav      = favorites.includes(id)
  const roomReviews = reviews.filter((r) => r.roomId === id)
  const avgRating  = roomReviews.length ? roomReviews.reduce((a, b) => a + b.rating, 0) / roomReviews.length : null

  const favCount = room ? room.baseFavCount + (isFav ? 1 : 0) : 0

  const hasConfirmed = reservations.some((r) => r.roomId === id && (r.status === 'confirmed' || r.status === 'payment_done'))

  if (loading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!room) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <EmptyState
          title="룸을 찾을 수 없습니다"
          description="요청하신 스터디룸이 존재하지 않습니다."
          action={{ label: '목록으로', onClick: () => navigate('/user/rooms') }}
        />
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-auto">
      <div className="max-w-3xl mx-auto px-4 md:px-6 py-6 md:py-8">

        {/* 뒤로가기 */}
        <button onClick={() => navigate('/user/rooms')} className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 transition-colors mb-5">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><polyline points="15 18 9 12 15 6" /></svg>
          목록으로 돌아가기
        </button>

        {/* Hero 카드 */}
        <div className="rounded-2xl p-5 md:p-6 mb-4" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
          <div className="flex items-start justify-between gap-4">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl flex items-center justify-center text-base font-bold shrink-0"
                style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)', color: '#fff' }}>
                {room.id}
              </div>
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <h1 className="text-xl font-bold text-gray-900">{room.name}</h1>
                  {room.cancelSalePrice && (
                    <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold" style={{ background: '#fef2f2', color: '#dc2626' }}>취소특가</span>
                  )}
                </div>
                <div className="flex items-center gap-2 mt-1 flex-wrap">
                  <span className="text-xs text-gray-500">최대 {room.capacity}명</span>
                  <span className="text-gray-200">·</span>
                  <Badge variant="roomStatus" value={room.status} />
                </div>
              </div>
            </div>
            {/* 위시리스트 */}
            <div className="flex flex-col items-center gap-0.5 shrink-0">
              <button onClick={() => toggleFavorite(id)} className="p-2 rounded-xl hover:bg-gray-50 transition-all hover:scale-110">
                <svg width="22" height="22" viewBox="0 0 24 24"
                  fill={isFav ? '#f59e0b' : 'none'} stroke={isFav ? '#f59e0b' : '#cbd5e1'} strokeWidth="2" strokeLinecap="round">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                </svg>
              </button>
              <span className="text-[10px] font-semibold tabular-nums" style={{ color: isFav ? '#b45309' : '#94a3b8' }}>{favCount}</span>
            </div>
          </div>

          {/* 가격 */}
          <div className="mt-4 pt-4 flex items-end justify-between" style={{ borderTop: '1px solid #f1f5f9' }}>
            <div>
              {discountedPrice ? (
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-2xl font-bold text-gray-900">{discountedPrice.toLocaleString()}원</span>
                    <span className="text-[11px] font-bold px-1.5 py-0.5 rounded" style={{ background: '#fee2e2', color: '#dc2626' }}>-{Math.round(discount * 100)}%</span>
                  </div>
                  <span className="text-sm text-gray-400 line-through">{room.price.toLocaleString()}원/시간</span>
                </div>
              ) : room.cancelSalePrice ? (
                <div>
                  <span className="text-2xl font-bold" style={{ color: '#dc2626' }}>{room.cancelSalePrice.toLocaleString()}원</span>
                  <span className="ml-2 text-sm text-gray-400 line-through">{room.price.toLocaleString()}원</span>
                  <div className="mt-0.5"><CancelSaleCountdown roomId={room.id} /></div>
                </div>
              ) : (
                <span className="text-2xl font-bold text-gray-900">{room.price.toLocaleString()}원<span className="text-sm font-normal text-gray-400">/시간</span></span>
              )}
            </div>
            {avgRating !== null && (
              <div className="text-right">
                <StarRating value={Math.round(avgRating)} size={14} />
                <div className="text-xs text-gray-400 mt-0.5">{avgRating.toFixed(1)} ({roomReviews.length}개 리뷰)</div>
              </div>
            )}
          </div>
        </div>

        {/* 시간대 현황 */}
        <div className="rounded-2xl p-5 mb-4" style={{ background: '#fff', border: '1px solid #e8edf5' }}>
          <h2 className="text-sm font-bold text-gray-700 mb-3">오늘 예약 현황</h2>
          <TimeSlotRow room={room} currentHour={displayHour} />
        </div>

        {/* 리뷰 */}
        <div className="rounded-2xl p-5 mb-6" style={{ background: '#fff', border: '1px solid #e8edf5' }}>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-sm font-bold text-gray-700">리뷰</h2>
            <button onClick={() => navigate(`/user/rooms/${id}/reviews`)}
              className="text-xs font-medium transition-colors hover:opacity-80" style={{ color: '#2d5a9e' }}>
              전체 보기 →
            </button>
          </div>
          {roomReviews.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-6">아직 리뷰가 없습니다.</p>
          ) : (
            <div className="flex flex-col gap-3">
              {roomReviews.slice(0, 3).map((rv) => (
                <div key={rv.id} className="rounded-xl p-4" style={{ background: '#f8fafc' }}>
                  <div className="flex items-center justify-between mb-1.5">
                    <StarRating value={rv.rating} size={12} />
                    <span className="text-xs text-gray-400">{rv.date}</span>
                  </div>
                  <p className="text-sm text-gray-700 leading-relaxed">{rv.content}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* CTA */}
        <div className="flex gap-3">
          {hasConfirmed && (
            <button onClick={() => navigate(`/user/rooms/${id}/reviews`)}
              className="flex-none px-5 py-3.5 rounded-2xl text-sm font-semibold transition-all"
              style={{ background: '#f8fafc', color: '#64748b', border: '1.5px solid #e2e8f0' }}>
              리뷰 작성
            </button>
          )}
          <button
            disabled={room.status === 'reserved'}
            onClick={() => navigate(`/user/rooms/${id}/reserve`)}
            className="flex-1 py-3.5 rounded-2xl text-sm font-bold text-white transition-all hover:opacity-90 disabled:opacity-40"
            style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)', boxShadow: room.status !== 'reserved' ? '0 4px 14px rgba(30,58,95,0.3)' : 'none' }}>
            {room.status === 'reserved' ? '현재 예약 불가' : '예약하기'}
          </button>
        </div>
      </div>
    </div>
  )
}
