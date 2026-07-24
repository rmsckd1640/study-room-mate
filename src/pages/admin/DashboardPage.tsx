import { useAuth } from '../../context/AuthContext'
import { ROOMS } from './AdminRoomsPage'

const STATUS_COLOR: Record<string, { color: string; bg: string; label: string }> = {
  pending:      { color: '#b45309', bg: '#fffbeb', label: '승인 대기' },
  confirmed:    { color: '#16a34a', bg: '#f0fdf4', label: '확정' },
  cancelled:    { color: '#6b7280', bg: '#f9fafb', label: '취소' },
  payment_done: { color: '#7c3aed', bg: '#f5f3ff', label: '결제 완료' },
  rejected:     { color: '#dc2626', bg: '#fef2f2', label: '거절' },
}

function StatCard({ label, value, sub, color }: { label: string; value: string | number; sub?: string; color: string; bg?: string }) {
  return (
    <div className="rounded-2xl p-5" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
      <div className="text-xs font-medium text-gray-500 mb-1">{label}</div>
      <div className="text-3xl font-bold mb-0.5" style={{ color, letterSpacing: '-0.03em' }}>{value}</div>
      {sub && <div className="text-xs text-gray-400">{sub}</div>}
    </div>
  )
}

export default function DashboardPage() {
  const { reservations } = useAuth()
  const allRes = reservations  // 관리자는 전체 예약 볼 수 있음

  const pending   = allRes.filter((r) => r.status === 'pending').length
  const confirmed = allRes.filter((r) => r.status === 'confirmed').length
  const totalRevenue = allRes.filter((r) => r.status === 'confirmed').reduce((a, r) => a + r.price, 0)

  // 방별 예약 건수
  const roomCounts = ROOMS.map((room) => ({
    ...room,
    count: allRes.filter((r) => r.roomId === room.id && r.status === 'confirmed').length,
  })).sort((a, b) => b.count - a.count)

  const maxCount = Math.max(...roomCounts.map((r) => r.count), 1)

  // 최근 예약 (최신 5건)
  const recent = [...allRes].sort((a, b) => b.id - a.id).slice(0, 5)

  return (
    <div className="flex-1 overflow-auto">
      <div className="max-w-6xl mx-auto px-6 py-8">

        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-900" style={{ letterSpacing: '-0.02em' }}>대시보드</h1>
          <p className="text-sm text-gray-500 mt-1">스터디룸 운영 현황을 한눈에 확인하세요</p>
        </div>

        {/* 핵심 통계 */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <StatCard label="전체 방" value={ROOMS.length} sub="등록된 스터디룸" color="#2d5a9e" bg="#eff6ff" />
          <StatCard label="승인 대기" value={pending} sub="처리 필요" color="#b45309" bg="#fffbeb" />
          <StatCard label="확정 예약" value={confirmed} sub="총 누적" color="#16a34a" bg="#f0fdf4" />
          <StatCard
            label="확정 매출 추정"
            value={totalRevenue.toLocaleString('ko-KR') + '원'}
            sub="확정 예약 기준"
            color="#7c3aed"
            bg="#f5f3ff"
          />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">

          {/* 방별 예약 건수 */}
          <div className="rounded-2xl p-6" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <h2 className="text-sm font-bold text-gray-900 mb-5">방별 확정 예약 건수</h2>
            <div className="flex flex-col gap-3">
              {roomCounts.map((room) => (
                <div key={room.id}>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-xs font-medium text-gray-700">{room.name}</span>
                    <span className="text-xs font-bold" style={{ color: '#2d5a9e' }}>{room.count}건</span>
                  </div>
                  <div className="h-2 rounded-full overflow-hidden" style={{ background: '#f1f5f9' }}>
                    <div
                      className="h-full rounded-full transition-all"
                      style={{
                        width: `${(room.count / maxCount) * 100}%`,
                        background: room.count > 0
                          ? 'linear-gradient(90deg, #2d5a9e, #60a5fa)'
                          : 'transparent',
                      }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 예약 상태 분포 */}
          <div className="rounded-2xl p-6" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <h2 className="text-sm font-bold text-gray-900 mb-5">예약 상태 분포</h2>
            <div className="grid grid-cols-2 gap-3">
              {Object.entries(STATUS_COLOR).map(([status, cfg]) => {
                const count = allRes.filter((r) => r.status === status).length
                return (
                  <div key={status} className="rounded-xl p-4" style={{ background: cfg.bg, border: `1px solid ${cfg.color}22` }}>
                    <div className="text-xs font-medium mb-1" style={{ color: cfg.color }}>{cfg.label}</div>
                    <div className="text-2xl font-bold" style={{ color: cfg.color }}>{count}</div>
                    <div className="text-xs mt-0.5" style={{ color: cfg.color, opacity: 0.7 }}>
                      {allRes.length ? Math.round((count / allRes.length) * 100) : 0}%
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>

        {/* 최근 예약 */}
        <div className="rounded-2xl overflow-hidden" style={{ border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
          <div className="px-5 py-4 flex items-center justify-between" style={{ background: '#f8fafc', borderBottom: '1px solid #e8edf5' }}>
            <h2 className="text-sm font-bold text-gray-900">최근 예약</h2>
            <span className="text-xs text-gray-400">최근 {recent.length}건</span>
          </div>
          <table className="w-full">
            <thead>
              <tr style={{ borderBottom: '1px solid #f1f5f9' }}>
                {['예약 ID', '방', '신청자', '날짜 / 시간', '금액', '상태'].map((h) => (
                  <th key={h} className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {recent.map((r, i) => {
                const s = STATUS_COLOR[r.status] ?? { color: '#6b7280', bg: '#f9fafb', label: r.status }
                return (
                  <tr key={r.id} className="hover:bg-gray-50 transition-colors"
                    style={{ borderBottom: i < recent.length - 1 ? '1px solid #f9fafb' : 'none', background: '#fff' }}>
                    <td className="px-5 py-3.5">
                      <span className="text-xs font-mono font-medium text-gray-500">#{String(r.id).padStart(4, '0')}</span>
                    </td>
                    <td className="px-5 py-3.5 text-sm font-medium text-gray-900 whitespace-nowrap">{r.roomName}</td>
                    <td className="px-5 py-3.5 text-sm text-gray-600">{r.userName}</td>
                    <td className="px-5 py-3.5 text-xs text-gray-600 whitespace-nowrap">
                      {r.date}<br />
                      <span className="text-gray-400">{r.startHour}:00–{r.endHour}:00</span>
                    </td>
                    <td className="px-5 py-3.5 text-sm font-semibold text-gray-900 whitespace-nowrap">{r.price.toLocaleString()}원</td>
                    <td className="px-5 py-3.5">
                      <span className="px-2.5 py-1 rounded-full text-xs font-semibold" style={{ background: s.bg, color: s.color }}>{s.label}</span>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
