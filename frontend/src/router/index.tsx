import { createBrowserRouter, Navigate } from 'react-router-dom'
import Layout from '@/components/layout/Layout'
import PrivateRoute from './PrivateRoute'

export const router = createBrowserRouter([
  {
    path: '/login',
    lazy: () => import('@/pages/LoginPage').then((m) => ({ Component: m.default })),
  },
  {
    path: '/',
    element: <PrivateRoute />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <Navigate to="/patients" replace /> },
          // TASK-05: 환자 관리
          {
            path: 'patients',
            lazy: () => import('@/pages/patients/PatientListPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'patients/new',
            lazy: () => import('@/pages/patients/PatientFormPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'patients/:id/edit',
            lazy: () => import('@/pages/patients/PatientFormPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'patients/:id',
            lazy: () => import('@/pages/patients/PatientDetailPage').then((m) => ({ Component: m.default })),
          },
          // TASK-05: 장치 관리
          {
            path: 'devices',
            lazy: () => import('@/pages/devices/DeviceListPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'devices/:id',
            lazy: () => import('@/pages/devices/DeviceDetailPage').then((m) => ({ Component: m.default })),
          },
          // TASK-06: 현황 모니터링
          {
            path: 'monitoring/assignments',
            lazy: () => import('@/pages/monitoring/AssignmentStatusPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'monitoring/summary',
            lazy: () => import('@/pages/monitoring/DailySummaryPage').then((m) => ({ Component: m.default })),
          },
          // TASK-07: 이력 관리
          {
            path: 'history/assignments',
            lazy: () => import('@/pages/history/AssignmentHistoryPage').then((m) => ({ Component: m.default })),
          },
          {
            path: 'history/biometric',
            lazy: () => import('@/pages/history/BiometricHistoryPage').then((m) => ({ Component: m.default })),
          },
        ],
      },
    ],
  },
])
