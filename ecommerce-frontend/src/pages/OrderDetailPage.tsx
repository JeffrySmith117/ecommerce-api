import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { findOrderById } from '../api/orders';
import { extractErrorMessage } from '../api/client';
import type { OrderResponse } from '../types';

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Pendente',
  CONFIRMED: 'Confirmado',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
};

export function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    findOrderById(Number(id))
      .then(setOrder)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <div className="page">Carregando pedido...</div>;
  if (error) return <div className="page"><p className="form-error">{error}</p></div>;
  if (!order) return null;

  return (
    <div className="page">
      <Link to="/orders">&larr; Voltar para meus pedidos</Link>
      <h1>Pedido #{order.id}</h1>
      <p>
        Status:{' '}
        <span className={`status-badge status-${order.status.toLowerCase()}`}>
          {STATUS_LABELS[order.status] ?? order.status}
        </span>
      </p>
      <p>Feito em: {new Date(order.createdAt).toLocaleString('pt-BR')}</p>

      <table className="orders-table">
        <thead>
          <tr>
            <th>Produto</th>
            <th>Quantidade</th>
            <th>Preço unitário</th>
            <th>Subtotal</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map((item) => (
            <tr key={item.id}>
              <td>{item.product.name}</td>
              <td>{item.quantity}</td>
              <td>{item.unitPrice.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</td>
              <td>{item.subtotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <p className="cart-total">
        Total: {order.total.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
      </p>
    </div>
  );
}
