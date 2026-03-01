import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { usePatientDetail, useCreatePatient, useUpdatePatient } from '@/hooks/usePatients'
import Button from '@/components/ui/Button'

const schema = z.object({
  name: z.string().min(2, '2자 이상 입력해 주세요.').max(50, '50자 이하로 입력해 주세요.'),
  birthDate: z.string().min(1, '생년월일을 입력해 주세요.'),
  gender: z.enum(['M', 'F'], { required_error: '성별을 선택해 주세요.' }),
  notes: z.string().optional(),
})

type FormData = z.infer<typeof schema>

export default function PatientFormPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEdit = !!id

  const { data: patient } = usePatientDetail(isEdit ? Number(id) : 0)
  const createMutation = useCreatePatient()
  const updateMutation = useUpdatePatient(Number(id))

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', birthDate: '', gender: 'M', notes: '' },
  })

  useEffect(() => {
    if (patient) {
      reset({
        name: patient.name,
        birthDate: patient.birthDate,
        gender: patient.gender,
        notes: patient.notes ?? '',
      })
    }
  }, [patient, reset])

  const onSubmit = async (data: FormData) => {
    if (isEdit) {
      await updateMutation.mutateAsync(data)
      navigate(`/patients/${id}`)
    } else {
      const res = await createMutation.mutateAsync(data)
      navigate(`/patients/${res.data.data!.id}`)
    }
  }

  const isPending = createMutation.isPending || updateMutation.isPending

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        <div style={{ width: 28, height: 28, borderRadius: '50%', background: 'var(--primary-bg)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13 }}>👤</div>
        <h1 style={{ fontSize: 15, fontWeight: 700 }}>{isEdit ? '환자 정보 수정' : '환자 신규 등록'}</h1>
      </div>

      <div style={{ background: 'var(--white)', border: '1px solid var(--gray-border)', borderRadius: 8, padding: 20, maxWidth: 560 }}>
        <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {/* 환자명 */}
          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
            <label className="form-label">환자명 <span style={{ color: 'var(--danger)' }}>*</span></label>
            <div>
              <input className={`form-input${errors.name ? ' error' : ''}`} {...register('name')} />
              {errors.name && <p style={{ fontSize: 11, color: 'var(--danger)', marginTop: 2 }}>{errors.name.message}</p>}
            </div>
          </div>

          {/* 생년월일 */}
          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
            <label className="form-label">생년월일 <span style={{ color: 'var(--danger)' }}>*</span></label>
            <div>
              <input type="date" className={`form-input${errors.birthDate ? ' error' : ''}`} {...register('birthDate')} />
              {errors.birthDate && <p style={{ fontSize: 11, color: 'var(--danger)', marginTop: 2 }}>{errors.birthDate.message}</p>}
            </div>
          </div>

          {/* 성별 */}
          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'center', gap: 10 }}>
            <label className="form-label">성별 <span style={{ color: 'var(--danger)' }}>*</span></label>
            <div style={{ display: 'flex', gap: 16 }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12.5, cursor: 'pointer' }}>
                <input type="radio" value="M" {...register('gender')} /> 남
              </label>
              <label style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 12.5, cursor: 'pointer' }}>
                <input type="radio" value="F" {...register('gender')} /> 여
              </label>
              {errors.gender && <p style={{ fontSize: 11, color: 'var(--danger)' }}>{errors.gender.message}</p>}
            </div>
          </div>

          {/* 특이사항 */}
          <div className="form-row" style={{ display: 'grid', gridTemplateColumns: '90px 1fr', alignItems: 'start', gap: 10 }}>
            <label className="form-label" style={{ paddingTop: 6 }}>특이사항</label>
            <textarea
              className="form-input"
              rows={3}
              style={{ height: 'auto', padding: '7px 10px', resize: 'none' }}
              {...register('notes')}
            />
          </div>

          {/* 버튼 */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 6 }}>
            <Button variant="outline" type="button" onClick={() => navigate(-1)}>취소</Button>
            <Button variant="primary" type="submit" loading={isPending}>
              {isEdit ? '수정' : '등록'}
            </Button>
          </div>
        </form>
      </div>
    </>
  )
}
