interface PaginationProps {
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void
}

export default function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null

  const getPages = (): (number | '...')[] => {
    const pages: (number | '...')[] = []
    const delta = 2

    for (let i = 1; i <= totalPages; i++) {
      if (i === 1 || i === totalPages || (i >= currentPage - delta && i <= currentPage + delta)) {
        pages.push(i)
      } else if (pages[pages.length - 1] !== '...') {
        pages.push('...')
      }
    }

    return pages
  }

  return (
    <div className="pagination">
      <span style={{ fontSize: 12, color: 'var(--text-sub)', marginRight: 8 }}>
        {currentPage} / {totalPages} 페이지
      </span>
      <button
        className="pg-btn"
        disabled={currentPage === 1}
        onClick={() => onPageChange(currentPage - 1)}
        style={{ opacity: currentPage === 1 ? 0.4 : 1 }}
      >
        ‹
      </button>
      {getPages().map((page, idx) =>
        page === '...' ? (
          <span key={`dots-${idx}`} className="pg-btn" style={{ border: 'none', cursor: 'default' }}>
            …
          </span>
        ) : (
          <button
            key={page}
            className={`pg-btn${page === currentPage ? ' active' : ''}`}
            onClick={() => onPageChange(page)}
          >
            {page}
          </button>
        )
      )}
      <button
        className="pg-btn"
        disabled={currentPage === totalPages}
        onClick={() => onPageChange(currentPage + 1)}
        style={{ opacity: currentPage === totalPages ? 0.4 : 1 }}
      >
        ›
      </button>
    </div>
  )
}
