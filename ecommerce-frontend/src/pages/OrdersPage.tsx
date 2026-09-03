import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { findMyOrders } from '../api/orders';
import { extractErrorMessage } from '../api/client';
import { Pagination } from '../components/Pagination';
import type { OrderResponse } from '../types';

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendente',
  CONFIRMED: 'Confirmado',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
};

export function OrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await findMyOrders(page, 10);
      setOrders(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  return (
    <div className="page">
      <h1>Meus pedidos</h1>
      {error && <p className="form-error">{error}</p>}

      {loading ? (
        <p>Carregando...</p>
      ) : orders.length === 0 ? (
        <p>Você ainda não fez nenhum pedido.</p>
      ) : (
        <table className="orders-table">
          <thead>
            <tr>
              <th>Pedido</th>
              <th>Data</th>
              <th>Status</th>
              <th>Total</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td>#{order.id}</td>
                <td>{new Date(order.createdAt).toLocaleString('pt-BR')}</td>
                <td>
                  <span className={`status-badge status-${order.status.toLowerCase()}`}>
                    {STATUS_LABELS[order.status] ?? order.status}
                  </span>
                </td>
                <td>{order.total.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</td>
                <td>
                  <Link to={`/orders/${order.id}`}>Ver detalhes</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
