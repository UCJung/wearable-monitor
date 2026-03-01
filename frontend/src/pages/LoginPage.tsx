import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import axiosInstance from '@/api/axiosInstance'
import { useAuthStore } from '@/stores/authStore'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setTokens, setUser, setRole } = useAuthStore()
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await axiosInstance.post('/auth/login', {
        username: userId,
        password,
        platform: 'WEB',
      })
      const data = res.data.data
      const { accessToken, refreshToken, role, username } = data

      if (role !== 'STAFF') {
        setError('웹은 의료진 계정만 로그인할 수 있습니다.')
        return
      }

      setTokens(accessToken, refreshToken)
      setRole(role)
      setUser({ id: username, name: username })
      navigate('/', { replace: true })
    } catch (err: any) {
      const code = err?.response?.data?.code
      if (code === 'STAFF_ONLY_LOGIN') {
        setError('웹은 의료진 계정만 로그인할 수 있습니다.')
      } else {
        setError('아이디 또는 비밀번호가 올바르지 않습니다.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(160deg, #f5f3ff 0%, #ffffff 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <form
        onSubmit={handleSubmit}
        style={{
          width: 360,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 0,
        }}
      >
        <span style={{ fontSize: 48, marginBottom: 16 }}>⌚</span>
        <h1 style={{ fontSize: 20, fontWeight: 700, marginBottom: 4 }}>웨어러블 모니터링</h1>
        <p style={{ fontSize: 12.5, color: 'var(--text-sub)', marginBottom: 32 }}>
          환자 건강 데이터 관리 시스템
        </p>

        <div style={{ width: '100%', marginBottom: 12 }}>
          <input
            className="form-input"
            type="text"
            placeholder="사용자 ID"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
          />
        </div>
        <div style={{ width: '100%', marginBottom: 8 }}>
          <input
            className="form-input"
            type="password"
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button
          type="submit"
          className="btn btn-primary"
          disabled={loading}
          style={{ width: '100%', height: 48, fontSize: 15, borderRadius: 12, marginTop: 8 }}
        >
          {loading ? '로그인 중...' : '로그인'}
        </button>

        {error && (
          <div
            style={{
              marginTop: 12,
              padding: '10px 14px',
              background: 'var(--danger-bg)',
              borderRadius: 6,
              color: 'var(--danger)',
              fontSize: 12.5,
              width: '100%',
              textAlign: 'center',
            }}
          >
            {error}
          </div>
        )}

        <p style={{ fontSize: 11.5, color: 'var(--text-sub)', marginTop: 16 }}>
          관리자에게 계정을 문의하세요.
        </p>
      </form>
    </div>
  )
}
