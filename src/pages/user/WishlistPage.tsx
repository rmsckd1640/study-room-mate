import { useNavigate } from 'react-router'
import { useAuth, GRADE_CONFIG } from '../../context/AuthContext'
import { ROOMS } from '../admin/AdminRoomsPage'
import { StarRating } from '../../components/rooms/StarRating'
import { EmptyState } from '../../components/ui/EmptyState'
import { Badge } from '../../components/ui/Badge'

export default function WishlistPage() {
  const navigate = useNavigate()
  const { favorites, toggleFavorite, grade, reviews } = useAuth()

  const wishRooms = ROOMS.filter((r) => favorites.includes(r.id))

  const discount = grade ? GRADE_CONFIG[grade].discount : 0
  const discountedPrice = (price: number) => discount > 0 ? Math.round(price * (1 - discount)) : null

  const avgRating = (roomId: number) => {
    const rs = reviews.filter((r) => r.roomId === roomId)
    return rs.length ? rs.reduce((a, b) => a + b.rating, 0) / rs.length : null
  }

  return (
    <div className="flex-1 overflow-auto">
      <div className="max-w-4xl mx-auto px-4 md:px-6 py-6 md:py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900" style={{ letterSpacing: '-0.02em' }}>위시리스트</h1>
            <p className="text-sm text-gray-500 mt-1">찜한 룸 {wishRooms.length}개</p>
          </div>
        </div>

        {wishRooms.length === 0 ? (
          <EmptyState
            icon={
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#94a3b8" strokeWidth="1.5" strokeLinecap="round">
                <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z" />
              </svg>
            }
            title="위시리스트가 비어있습니다"
            description="룸 목록에서 마음에 드는 방에 별표를 눌러 찜해보세요."
            action={{ label: '룸 목록 보기', onClick: () => navigate('/user/rooms') }}
          />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {wishRooms.map((room) => {
              const dp  = discountedPrice(room.price)
              const avg = avgRating(room.id)
              const reviewCount = reviews.filter((r) => r.roomId === room.id).length
              return (
                <div key={room.id} className="rounded-2xl overflow-hidden flex flex-col"
                  style={{ background: '#fff', border: '1.5px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.04)' }}>

                  {/* 상단 */}
                  <div className="p-4 flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl flex items-center justify-center text-sm font-bold shrink-0"
                        style={{ background: '#eff6ff', color: '#2d5a9e' }}>{room.id}</div>
                      <div>
                        <button onClick={() => navigate(`/user/rooms/${room.id}`)}
                          className="text-sm font-bold text-gray-900 hover:text-blue-700 transition-colors text-left">
                          {room.name}
                        </button>
                        <div className="text-xs text-gray-500 mt-0.5">최대 {room.capacity}명</div>
                      </div>
                    </div>
                    {/* 찜 제거 */}
                    <button onClick={() => toggleFavorite(room.id)}
                      className="p-1.5 rounded-lg hover:bg-red-50 transition-colors shrink-0" title="위시리스트에서 제거">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="#f59e0b" stroke="#f59e0b" strokeWidth="2" strokeLinecap="round">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
                      </svg>
                    </button>
                  </div>

                  {/* 가격 + 상태 */}
                  <div className="px-4 pb-3 flex items-center justify-between">
                    <div>
                      {dp ? (
                        <div>
                          <span className="text-base font-bold text-gray-900">{dp.toLocaleString()}원</span>
                          <span className="ml-1.5 text-xs text-gray-400 line-through">{room.price.toLocaleString()}원</span>
                          <span className="ml-1 text-[10px] font-bold px-1 py-0.5 rounded" style={{ background: '#fee2e2', color: '#dc2626' }}>-{Math.round(discount * 100)}%</span>
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
                    <Badge variant="roomStatus" value={room.status} />
                  </div>

                  {/* 평점 */}
                  {avg !== null && (
                    <div className="px-4 pb-3 flex items-center gap-2">
                      <StarRating value={Math.round(avg)} size={10} />
                      <span className="text-[10px] text-gray-400">{avg.toFixed(1)} ({reviewCount}개)</span>
                    </div>
                  )}

                  {/* 버튼 */}
                  <div className="px-4 pb-4 mt-auto flex gap-2">
                    <button onClick={() => navigate(`/user/rooms/${room.id}`)}
                      className="flex-1 py-2 rounded-xl text-xs font-semibold transition-all"
                      style={{ background: '#f8fafc', color: '#64748b', border: '1px solid #e2e8f0' }}>
                      상세보기
                    </button>
                    <button onClick={() => navigate(`/user/rooms/${room.id}/reserve`)}
                      disabled={room.status === 'reserved'}
                      className="flex-1 py-2 rounded-xl text-xs font-semibold text-white transition-all hover:opacity-90 disabled:opacity-40"
                      style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
                      예약하기
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
