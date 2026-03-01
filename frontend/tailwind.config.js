/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: 'var(--primary)',
        'primary-dark': 'var(--primary-dark)',
        'primary-bg': 'var(--primary-bg)',
        accent: 'var(--accent)',
        ok: 'var(--ok)',
        'ok-bg': 'var(--ok-bg)',
        warn: 'var(--warn)',
        'warn-bg': 'var(--warn-bg)',
        danger: 'var(--danger)',
        'danger-bg': 'var(--danger-bg)',
        'gray-border': 'var(--gray-border)',
        'gray-light': 'var(--gray-light)',
        text: 'var(--text)',
        'text-sub': 'var(--text-sub)',
      },
    },
  },
  plugins: [],
}
