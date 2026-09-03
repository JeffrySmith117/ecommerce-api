import { useCallback, useEffect, useState } from 'react';
import { findProducts, findCategories } from '../api/products';
import { addItem } from '../api/cart';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { Pagination } from '../components/Pagination';
import type { CategoryResponse, ProductResponse } from '../types';

export function ProductsPage() {
  const { isAuthenticated } = useAuth();

  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await findProducts({ page, size: 12, search, categoryId });
      setProducts(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [page, search, categoryId]);

  useEffect(() => {
    findCategories()
      .then(setCategories)
      .catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  function handleSearchSubmit(event: React.FormEvent) {
    event.preventDefault();
    setPage(0);
    loadProducts();
  }

  async function handleAddToCart(productId: number) {
    setFeedback(null);
    try {
      await addItem(productId, 1);
      setFeedback('Produto adicionado ao carrinho.');
    } catch (err) {
      setFeedback(extractErrorMessage(err));
    }
  }

  return (
    <div className="page">
      <h1>Produtos</h1>

      <form className="filters" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="Buscar por nome..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select
          value={categoryId ?? ''}
          onChange={(e) => {
            setCategoryId(e.target.value ? Number(e.target.value) : undefined);
            setPage(0);
          }}
        >
          <option value="">Todas as categorias</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <button type="submit">Buscar</button>
      </form>

      {feedback && <p className="form-feedback">{feedback}</p>}
      {error && <p className="form-error">{error}</p>}

      {loading ? (
        <p>Carregando...</p>
      ) : products.length === 0 ? (
        <p>Nenhum produto encontrado.</p>
      ) : (
        <div className="product-grid">
          {products.map((product) => (
            <div className="product-card" key={product.id}>
              <h3>{product.name}</h3>
              {product.category && <span className="product-category">{product.category.name}</span>}
              {product.description && <p className="product-description">{product.description}</p>}
              <p className="product-price">
                {product.price.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
              </p>
              <p className="product-stock">Estoque: {product.stockQty}</p>
              <button
                disabled={!isAuthenticated || product.stockQty === 0}
                onClick={() => handleAddToCart(product.id)}
              >
                {isAuthenticated ? 'Adicionar ao carrinho' : 'Entre para comprar'}
              </button>
            </div>
          ))}
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
