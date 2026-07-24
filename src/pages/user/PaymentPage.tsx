import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router'
import { useAuth } from '../../context/AuthContext'

interface PaymentState {
  roomId: number
  roomName: string
  date: string
  startHour: number
  endHour: number
  selectedHours: number[]
  price: number
  basePrice: number
  discount: number
  grade: string | null
}

/* ── TossPay 위젯 모달 ────────────────────────────────────── */
function TossPayWidget({ amount, onClose, onSuccess }: { amount: number; onClose: () => void; onSuccess: () => void }) {
  const [stage, setStage] = useState<'select' | 'confirm' | 'processing' | 'done'>('select')

  const handlePay = () => {
    setStage('processing')
    setTimeout(() => setStage('done'), 2000)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: 'rgba(0,0,0,0.55)' }} onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="w-full max-w-sm mx-4 rounded-3xl overflow-hidden" style={{ background: '#fff', boxShadow: '0 24px 64px rgba(0,0,0,0.25)' }}>
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 py-4" style={{ borderBottom: '1px solid #f1f5f9' }}>
          <div className="flex items-center gap-2.5">
            {/* Toss Pay 로고 모방 */}
            <div className="w-8 h-8 rounded-full flex items-center justify-center" style={{ background: '#0064FF' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="white">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/>
              </svg>
            </div>
            <span className="font-bold text-gray-900 text-base">토스페이</span>
          </div>
          {stage !== 'processing' && stage !== 'done' && (
            <button onClick={onClose} className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#6b7280" strokeWidth="2.5" strokeLinecap="round">
                <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          )}
        </div>

        <div className="px-6 py-6">
          {stage === 'select' && (
            <div>
              <p className="text-xs text-gray-400 mb-1">결제 금액</p>
              <p className="text-3xl font-bold text-gray-900 mb-6">{amount.toLocaleString()}<span className="text-xl">원</span></p>

              <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-3">결제 수단 선택</p>
              <div className="space-y-2 mb-6">
                {[
                  { id: 'toss', label: '토스머니', sub: '잔액 충분' },
                  { id: 'card', label: '신용/체크카드', sub: '간편결제' },
                  { id: 'bank', label: '계좌이체', sub: '실시간 이체' },
                ].map((method) => (
                  <label key={method.id} className="flex items-center gap-3 p-3.5 rounded-2xl cursor-pointer transition-all"
                    style={{ border: `2px solid ${method.id === 'toss' ? '#0064FF' : '#e8edf5'}`, background: method.id === 'toss' ? '#f0f6ff' : '#fff' }}>
                    <input type="radio" name="method" defaultChecked={method.id === 'toss'} className="sr-only" />
                    <div className="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0"
                      style={{ borderColor: method.id === 'toss' ? '#0064FF' : '#d1d5db' }}>
                      {method.id === 'toss' && <div className="w-2.5 h-2.5 rounded-full" style={{ background: '#0064FF' }} />}
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-semibold text-gray-900">{method.label}</p>
                      <p className="text-xs text-gray-400">{method.sub}</p>
                    </div>
                  </label>
                ))}
              </div>

              <button onClick={() => setStage('confirm')} className="w-full py-3.5 rounded-2xl text-sm font-bold text-white transition-all hover:opacity-90"
                style={{ background: '#0064FF' }}>
                다음
              </button>
            </div>
          )}

          {stage === 'confirm' && (
            <div>
              <p className="text-xs text-gray-400 mb-1">최종 결제</p>
              <p className="text-3xl font-bold text-gray-900 mb-2">{amount.toLocaleString()}<span className="text-xl">원</span></p>
              <div className="rounded-2xl p-4 mb-6" style={{ background: '#f8fafc', border: '1px solid #e8edf5' }}>
                <div className="flex justify-between text-sm mb-1.5">
                  <span className="text-gray-500">결제 수단</span>
                  <span className="font-semibold text-gray-900">토스머니</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">결제 후 잔액</span>
                  <span className="font-semibold text-gray-900">{(50000 - amount).toLocaleString()}원</span>
                </div>
              </div>
              <button onClick={handlePay} className="w-full py-3.5 rounded-2xl text-sm font-bold text-white transition-all hover:opacity-90"
                style={{ background: '#0064FF' }}>
                {amount.toLocaleString()}원 결제하기
              </button>
              <button onClick={() => setStage('select')} className="w-full mt-2 py-2.5 rounded-2xl text-sm font-medium text-gray-500 hover:bg-gray-50 transition-colors">
                이전으로
              </button>
            </div>
          )}

          {stage === 'processing' && (
            <div className="flex flex-col items-center py-8">
              <div className="w-14 h-14 rounded-full flex items-center justify-center mb-4 animate-spin" style={{ border: '3px solid #e8edf5', borderTopColor: '#0064FF' }} />
              <p className="text-base font-bold text-gray-900 mb-1">결제 처리 중...</p>
              <p className="text-sm text-gray-400">잠시만 기다려주세요</p>
            </div>
          )}

          {stage === 'done' && (
            <div className="flex flex-col items-center py-8">
              <div className="w-14 h-14 rounded-full flex items-center justify-center mb-4" style={{ background: '#0064FF' }}>
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3" strokeLinecap="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <p className="text-base font-bold text-gray-900 mb-1">결제 완료!</p>
              <p className="text-sm text-gray-400 mb-6">{amount.toLocaleString()}원이 결제되었습니다</p>
              <button onClick={onSuccess} className="w-full py-3.5 rounded-2xl text-sm font-bold text-white"
                style={{ background: '#0064FF' }}>
                확인
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

/* ── 메인 ────────────────────────────────────────────────── */
export default function PaymentPage() {
  const location = useLocation()
  const navigate  = useNavigate()
  const { addReservation } = useAuth()

  const state = location.state as PaymentState | undefined
  const [allAgree, setAllAgree] = useState(false)
  const [agree1, setAgree1]     = useState(false)
  const [agree2, setAgree2]     = useState(false)
  const [showWidget, setShowWidget] = useState(false)
  const [done, setDone]         = useState(false)

  if (!state) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center gap-4">
        <p className="text-sm text-gray-400">결제 정보를 찾을 수 없습니다.</p>
        <button onClick={() => navigate('/user/rooms')} className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white"
          style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
          목록으로
        </button>
      </div>
    )
  }

  const { roomId, roomName, date, startHour, endHour, selectedHours, price, basePrice, discount } = state
  const slotLabel = (h: number) => `${h}:00~${h + 1}:00`

  const handleAllAgree = (v: boolean) => { setAllAgree(v); setAgree1(v); setAgree2(v) }

  const canPay = agree1 && agree2

  const handleSuccess = () => {
    addReservation({ roomId, roomName, date, startHour, endHour, price })
    setShowWidget(false)
    setDone(true)
  }

  if (done) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center px-6">
        <div className="w-20 h-20 rounded-full flex items-center justify-center mb-6" style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5" strokeLinecap="round">
            <polyline points="20 6 9 17 4 12" />
          </svg>
        </div>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">예약이 신청되었습니다!</h2>
        <p className="text-sm text-gray-500 mb-1">{roomName} · {date}</p>
        <p className="text-sm text-gray-500 mb-8">{selectedHours.map(slotLabel).join(', ')}</p>
        <div className="text-xl font-bold mb-8" style={{ color: '#1e3a5f' }}>{price.toLocaleString()}원 결제 완료</div>
        <div className="flex gap-3">
          <button onClick={() => navigate('/user/rooms')} className="px-5 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-100 transition-colors" style={{ border: '1.5px solid #e2e8f0' }}>
            목록으로
          </button>
          <button onClick={() => navigate('/user/reservations')} className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white"
            style={{ background: 'linear-gradient(135deg, #1e3a5f, #2d5a9e)' }}>
            예약 내역
          </button>
        </div>
      </div>
    )
  }

  return (
    <>
      {showWidget && (
        <TossPayWidget amount={price} onClose={() => setShowWidget(false)} onSuccess={handleSuccess} />
      )}

      <div className="flex-1 overflow-auto">
        <div className="max-w-xl mx-auto px-6 py-8">
          {/* 뒤로가기 */}
          <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 transition-colors mb-6">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><polyline points="15 18 9 12 15 6" /></svg>
            이전으로
          </button>

          <h1 className="text-2xl font-bold text-gray-900 mb-6" style={{ letterSpacing: '-0.02em' }}>결제</h1>

          {/* 주문 정보 */}
          <div className="rounded-2xl p-5 mb-4" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <h2 className="text-sm font-bold text-gray-700 mb-4">상품 정보</h2>
            <div className="flex flex-col gap-0">
              {[
                { label: '스터디룸', value: roomName },
                { label: '날짜', value: date },
                { label: '시간', value: selectedHours.map(slotLabel).join(', ') },
                { label: '총 시간', value: `${selectedHours.length}시간` },
              ].map(({ label, value }) => (
                <div key={label} className="flex justify-between py-2.5" style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <span className="text-sm text-gray-500">{label}</span>
                  <span className="text-sm font-semibold text-gray-900">{value}</span>
                </div>
              ))}
              <div className="flex justify-between py-2.5" style={{ borderBottom: '1px solid #f1f5f9' }}>
                <span className="text-sm text-gray-500">기본 요금</span>
                <span className="text-sm text-gray-900">{basePrice.toLocaleString()}원</span>
              </div>
              {discount > 0 && (
                <div className="flex justify-between py-2.5" style={{ borderBottom: '1px solid #f1f5f9' }}>
                  <span className="text-sm text-gray-500">등급 할인 ({Math.round(discount * 100)}%)</span>
                  <span className="text-sm" style={{ color: '#16a34a' }}>-{(basePrice - price).toLocaleString()}원</span>
                </div>
              )}
              <div className="flex justify-between pt-4">
                <span className="font-bold text-gray-900">결제 금액</span>
                <span className="text-xl font-bold" style={{ color: '#1e3a5f' }}>{price.toLocaleString()}원</span>
              </div>
            </div>
          </div>

          {/* 결제 수단 */}
          <div className="rounded-2xl p-5 mb-4" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <h2 className="text-sm font-bold text-gray-700 mb-3">결제 방법</h2>
            <div className="flex items-center gap-3 p-4 rounded-xl" style={{ background: '#f0f6ff', border: '2px solid #0064FF' }}>
              <div className="w-9 h-9 rounded-full flex items-center justify-center shrink-0" style={{ background: '#0064FF' }}>
                <span className="text-white text-xs font-black">T</span>
              </div>
              <div>
                <p className="text-sm font-bold text-gray-900">토스페이</p>
                <p className="text-xs text-gray-500">토스머니 · 카드 · 계좌이체</p>
              </div>
              <div className="ml-auto">
                <div className="w-5 h-5 rounded-full border-2 flex items-center justify-center" style={{ borderColor: '#0064FF' }}>
                  <div className="w-2.5 h-2.5 rounded-full" style={{ background: '#0064FF' }} />
                </div>
              </div>
            </div>
          </div>

          {/* 약관 동의 */}
          <div className="rounded-2xl p-5 mb-6" style={{ background: '#fff', border: '1px solid #e8edf5', boxShadow: '0 1px 4px rgba(0,0,0,0.05)' }}>
            <h2 className="text-sm font-bold text-gray-700 mb-3">약관 동의</h2>
            {/* 전체 동의 */}
            <label className="flex items-center gap-3 p-3.5 rounded-xl cursor-pointer mb-3"
              style={{ background: allAgree ? '#f0fdf4' : '#f8fafc', border: `1.5px solid ${allAgree ? '#16a34a' : '#e2e8f0'}` }}>
              <div onClick={() => handleAllAgree(!allAgree)}
                className="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-all"
                style={{ borderColor: allAgree ? '#16a34a' : '#d1d5db', background: allAgree ? '#16a34a' : '#fff' }}>
                {allAgree && <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3" strokeLinecap="round"><polyline points="20 6 9 17 4 12" /></svg>}
              </div>
              <span className="text-sm font-bold text-gray-900">전체 동의</span>
            </label>
            <div className="space-y-2 pl-1">
              {[
                { checked: agree1, setter: setAgree1, label: '[필수] 결제 서비스 이용 약관' },
                { checked: agree2, setter: setAgree2, label: '[필수] 개인정보 처리 동의' },
              ].map(({ checked, setter, label }) => (
                <label key={label} className="flex items-center gap-3 cursor-pointer py-1.5">
                  <div onClick={() => {
                    const next = !checked
                    setter(next)
                    if (!next) setAllAgree(false)
                    else if (next && (label.includes('이용 약관') ? agree2 : agree1)) setAllAgree(true)
                  }}
                    className="w-4 h-4 rounded border-2 flex items-center justify-center shrink-0 transition-all"
                    style={{ borderColor: checked ? '#1e3a5f' : '#d1d5db', background: checked ? '#1e3a5f' : '#fff' }}>
                    {checked && <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3.5" strokeLinecap="round"><polyline points="20 6 9 17 4 12" /></svg>}
                  </div>
                  <span className="text-sm text-gray-700">{label}</span>
                  <button className="ml-auto text-xs text-gray-400 underline hover:text-gray-600">보기</button>
                </label>
              ))}
            </div>
          </div>

          {/* 결제하기 버튼 */}
          <button disabled={!canPay} onClick={() => setShowWidget(true)}
            className="w-full py-4 rounded-2xl text-base font-bold text-white transition-all disabled:opacity-40"
            style={{ background: canPay ? '#0064FF' : '#94a3b8', boxShadow: canPay ? '0 8px 20px rgba(0,100,255,0.3)' : 'none' }}>
            {price.toLocaleString()}원 결제하기
          </button>
          {!canPay && (
            <p className="text-center text-xs text-gray-400 mt-2">필수 약관에 동의해주세요</p>
          )}
        </div>
      </div>
    </>
  )
}
