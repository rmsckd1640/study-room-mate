import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useAuth, type ReservationStatus } from '../../context/AuthContext'
import { Badge } from '../../components/ui/Badge'
import { EmptyState } from '../../components/ui/EmptyState'
import { useToast } from '../../context/ToastContext'

const STATUS_TABS: { key: ReservationStatus | 'all'; label: string }[] = [
  { key: 'all',          label: '전체' },
  { key: 'pending',      label: '대기 중' },
  { key: 'confirmed',    label: '확정' },
  { key: 'payment_done', label: '결제 완료' },
  { key: 'cancelled',    label: '취소됨' },
  { key: 'rejected',     label: '거절됨' },
]

/* ── 취소 사유 모달 ── */
function CancelModal({ onConfirm, onClose }: { onConfirm: (reason: string) => void; onClose: () => void }) {
  const [reason, setReason] = useState('')
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4" style={{ background: 'rgba(0,0,0,0.45)' }}
      onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="w-full max-w-sm rounded-2xl overflow-hidden" style={{ background: '#fff', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}>
        <div className="flex items-center justify-between px-6 py-4" style={{ borderBottom: '1px solid #f1f5f9' }}>
          <h3 className="text-base font-bold text-gray-900">예약 취소</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>
        <div className="px-6 py-5">
          <p className="text-sm text-gray-600 mb-3">취소 사유를 입력해주세요. (선택)</p>
          <textarea
            value={reason} onChange={(e) => setReason(e.target.value)}
            rows={4} placeholder="예: 일정 변경으로 인해 취소합니다."
            className="w-full px-4 py-3 rounded-xl text-sm text-gray-900 outline-none resize-none"
            style={{ background: '#f8fafc', border: '1.5px solid #e2e8f0' }}
          />
        </div>
        <div className="flex gap-2 px-6 pb-5">
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:bg-gray-100 transition-all">
            닫기
          </button>
          <button onClick={() => onConfirm(reason)}
            className="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white transition-all hover:opacity-90"
            style={{ background: 'linear-gradient(135deg, #dc2626, #ef4444)' }}>
            취소 확정
          </button>
        </div>
      </div>
    </div>
  )
}

export default function ReservationHistoryPage() {
  const navigate = useNavigate()
  const { reservations, cancelReservation, username } = useAuth()
  const { showToast } = useToast()

  const [activeTab, setActiveTab]   = useState<ReservationStatus | 'all'>('all')
  const [cancelId, setCancelId]     = useState<number | null>(null)

  const myReservations = reservations.filter((r) => r.userName === username)
  const filtered = activeTab === 'all' ? myReservations : myReservations.filter((r) => r.status === activeTab)

  const handleCancel = (reason: string) => {
    if (cancelId == null) return
    cancelReservation(cancelId, reason)
    setCancelId(null)
    showToast('예약이 취소되었습니다.', 'success')
  }

  return (
    <>
      {cancelId !== null && (
        <CancelModal onConfirm={handleCancel} onClose={() => setCancelId(null)} />
      )}

      <div className="flex-1 overflow-auto">
        <div className="max-w-3xl mx-auto px-4 md:px-6 py-6 md:py-8">

          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-2xl font-bold text-gray-900" style={{ letterSpacing: '-0.02em' }}>예약 내역</h1>
              <p className="text-sm text-gray-500 mt-1">총 {myReservations.length}건</p>
            </div>
          </div>

          {/* 상태 필터 탭 */}
          <div className="flex gap-2 overflow-x-auto pb-2 mb-5" style={{ scrollbarWidth: 'none' }}>
            {STATUS_TABS.map((tab) => {
              const active = activeTab === tab.key
              const count  = tab.key === 'all' ? myReservations.length : myReservations.filter((r) => r.status === tab.key).length
              return (
                <button key={tab.key} onClick={() => setActiveTab(tab.key)}
                  className="flex items-center gap-1.5 px-4 py-2 rounded-full text-xs font-semibold transition-all whitespace-nowrap shrink-0"
                  style={{
                    background: active ? '#1e3a5f' : '#f8fafc',
                    color: active ? '#fff' : '#64748b',
                    border: `1.5px solid ${active ? '#1e3a5f' : '#e2e8f0'}`,
                  }}>
                  {tab.label}
                  {count > 0 && (
                    <span className="w-4 h-4 rounded-full flex items-center justify-center text-[9px] font-bold"
                      style={{ background: active ? 'rgba(255,255,255,0.2)' : '#e2e8f0', color: active ? '#fff' : '#64748b' }}>
                      {count}
                    </span>
                  )}
                </button>
              )
            })}
          </div>

          {/* 리스트 */}
          {filtered.length === 0 ? (
            <EmptyState
              title="예약 내역이 없습니다"
              description={activeTab === 'all' ? '스터디룸을 예약해보세요.' : `${STATUS_TABS.find(t => t.key === activeTab)?.label} 상태의 예약이 없습니다.`}
              action={activeTab === 'all' ? { label: '룸 목록 보기', onClick: () => navigate('/user/rooms') } : undefined}
            />
          ) : (
            <div className="flex flex-col gap-3">
              {filtered.map((r) => (
                <div key={r.id} className="rounded-2xl p-4 md:p-5"
                  style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.04)' }}>
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-xs font-mono text-gray-400">#{String(r.id).padStart(4, '0')}</span>
                        <h3 className="text-sm font-bold text-gray-900">{r.roomName}</h3>
                      </div>
                      <div className="flex items-center gap-2 mt-1 text-xs text-gray-500 flex-wrap">
                        <span>{r.date}</span>
                        <span>·</span>
                        <span>{r.startHour}:00 ~ {r.endHour}:00</span>
                        <span>·</span>
                        <span className="font-semibold text-gray-900">{r.price.toLocaleString()}원</span>
                      </div>
                    </div>
                    <Badge variant="reservationStatus" value={r.status} />
                  </div>

                  {r.cancelReason && (
                    <div className="rounded-xl px-3 py-2 mb-3 text-xs text-gray-500" style={{ background: '#f8fafc', border: '1px solid #f1f5f9' }}>
                      취소 사유: {r.cancelReason}
                    </div>
                  )}

                  <div className="flex items-center gap-2">
                    <button onClick={() => navigate(`/user/rooms/${r.roomId}`)}
                      className="px-3 py-1.5 rounded-lg text-xs font-medium transition-colors"
                      style={{ background: '#f8fafc', color: '#64748b', border: '1px solid #e2e8f0' }}>
                      룸 보기
                    </button>
                    {r.status === 'confirmed' && (
                      <button onClick={() => setCancelId(r.id)}
                        className="px-3 py-1.5 rounded-lg text-xs font-semibold transition-all hover:opacity-90"
                        style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #fecaca' }}>
                        취소하기
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}
