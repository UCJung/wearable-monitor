import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Header from './Header'
import ToastContainer from '@/components/ui/ToastContainer'

export default function Layout() {
  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Sidebar />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Header />
        <main
          style={{
            flex: 1,
            padding: 'var(--content-pad)',
            background: 'var(--gray-light)',
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 'var(--content-gap)',
          }}
        >
          <Outlet />
        </main>
      </div>
      <ToastContainer />
    </div>
  )
}
