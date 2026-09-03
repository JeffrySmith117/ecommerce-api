interface PaginationProps {
  page: number; // 0-based
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="pagination">
      <button disabled={page === 0} onClick={() => onPageChange(page - 1)}>
        Anterior
      </button>
      <span>
        Página {page + 1} de {totalPages}
      </span>
      <button disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)}>
        Próxima
      </button>
    </div>
  );
}
