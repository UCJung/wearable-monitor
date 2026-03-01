import { useLocation } from 'react-router-dom'
import { useAuthStore } from '@/stores/authStore'

const breadcrumbMap: Record<string, string[]> = {
  '/patients': ['기준데이터', '환자 정보 관리'],
  '/devices': ['기준데이터', '장치 정보 관리'],
  '/monitoring/assignments': ['현황모니터링', '장치 할당 현황'],
  '/monitoring/summary': ['현황모니터링', '데이터 현황 집계'],
  '/history/assignments': ['이력관리', '할당·해제 이력'],
  '/history/biometric': ['이력관리', '수집 데이터 이력'],
}

export default function Header() {
  const { pathname } = useLocation()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  // 동적 라우트 매칭: /patients/:id, /patients/new 등
  const matchedKey = Object.keys(breadcrumbMap).find(
    (key) => pathname === key || pathname.startsWith(key + '/')
  )
  const breadcrumbs = matchedKey ? breadcrumbMap[matchedKey] : []

  return (
    <header
      style={{
        height: 'var(--header-h)',
        background: '#ffffff',
        borderBottom: '1px solid var(--gray-border)',
        padding: '0 20px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0,
      }}
    >
      {/* 브레드크럼 */}
      <nav style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12.5 }}>
        {breadcrumbs.map((crumb, i) => (
          <span key={crumb} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            {i > 0 && <span style={{ color: 'var(--text-sub)' }}>/</span>}
            <span
              style={{
                color: i === breadcrumbs.length - 1 ? 'var(--text)' : 'var(--text-sub)',
                fontWeight: i === breadcrumbs.length - 1 ? 600 : 400,
              }}
            >
              {crumb}
            </span>
          </span>
        ))}
      </nav>

      {/* 사용자 정보 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        {user && (
          <span style={{ fontSize: 12.5, color: 'var(--text-sub)' }}>
            {user.name}
          </span>
        )}
        <button
          className="btn btn-outline"
          onClick={logout}
          style={{ height: 26, fontSize: 11, padding: '0 10px' }}
        >
          로그아웃
        </button>
      </div>
    </header>
  )
}
