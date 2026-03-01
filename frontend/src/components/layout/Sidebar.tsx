import { NavLink } from 'react-router-dom'

const menuGroups = [
  {
    title: '기준데이터',
    items: [
      { to: '/patients', label: '환자 정보 관리' },
      { to: '/devices', label: '장치 정보 관리' },
    ],
  },
  {
    title: '현황모니터링',
    items: [
      { to: '/monitoring/assignments', label: '장치 할당 현황' },
      { to: '/monitoring/summary', label: '데이터 현황 집계' },
    ],
  },
  {
    title: '이력관리',
    items: [
      { to: '/history/assignments', label: '할당·해제 이력' },
      { to: '/history/biometric', label: '수집 데이터 이력' },
    ],
  },
]

export default function Sidebar() {
  return (
    <aside
      style={{
        width: 'var(--sidebar-w)',
        background: 'var(--sidebar-bg)',
        height: '100vh',
        overflowY: 'auto',
        flexShrink: 0,
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      {/* 로고 */}
      <div
        style={{
          height: 'var(--header-h)',
          padding: '0 16px',
          borderBottom: '1px solid #2a3045',
          display: 'flex',
          alignItems: 'center',
          gap: 8,
        }}
      >
        <span style={{ fontSize: 20 }}>⌚</span>
        <span style={{ color: '#fff', fontWeight: 700, fontSize: 13 }}>
          웨어러블 모니터
        </span>
      </div>

      {/* 메뉴 */}
      <nav style={{ paddingTop: 8 }}>
        {menuGroups.map((group) => (
          <div key={group.title}>
            <p
              style={{
                padding: '8px 16px 4px',
                fontSize: 10,
                fontWeight: 600,
                color: '#4a5470',
                letterSpacing: '0.8px',
                textTransform: 'uppercase',
              }}
            >
              {group.title}
            </p>
            {group.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                style={({ isActive }) => ({
                  display: 'flex',
                  alignItems: 'center',
                  padding: '7px 16px',
                  fontSize: 12.5,
                  color: isActive ? '#ffffff' : '#8896b3',
                  background: isActive ? '#252d48' : 'transparent',
                  borderLeft: isActive ? '3px solid var(--primary)' : '3px solid transparent',
                  fontWeight: isActive ? 500 : 400,
                  textDecoration: 'none',
                  transition: 'all 0.15s',
                })}
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
    </aside>
  )
}
