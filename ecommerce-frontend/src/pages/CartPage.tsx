import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCart, updateItem, removeItem, clearCart } from '../api/cart';
import { createOrder } from '../api/orders';
import { extractErrorMessage } from '../api/client';
import type { CartResponse } from '../types';

export function CartPage() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const loadCart = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setCart(await getCart());
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  async function handleQuantityChange(itemId: number, productId: number, quantity: number) {
    if (quantity < 1) return;
    try {
      setCart(await updateItem(itemId, productId, quantity));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleRemove(itemId: number) {
    try {
      await removeItem(itemId);
      await loadCart();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleClearCart() {
    try {
      await clearCart();
      await loadCart();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  }

  async function handleCheckout() {
    setSubmitting(true);
    setError(null);
    try {
      const order = await createOrder();
      navigate(`/orders/${order.id}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <div className="page">Carregando carrinho...</div>;

  return (
    <div className="page">
      <h1>Meu carrinho</h1>
      {error && <p className="form-error">{error}</p>}

      {!cart || cart.items.length === 0 ? (
        <p>Seu carrinho está vazio.</p>
      ) : (
        <>
          <table className="cart-table">
            <thead>
              <tr>
                <th>Produto</th>
                <th>Quantidade</th>
                <th>Subtotal</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {cart.items.map((item) => (
                <tr key={item.id}>
                  <td>{item.product.name}</td>
                  <td>
                    <input
                      type="number"
                      min={1}
                      max={item.product.stockQty}
                      value={item.quantity}
                      onChange={(e) =>
                        handleQuantityChange(item.id, item.product.id, Number(e.target.value))
                      }
                    />
                  </td>
                  <td>
                    {item.subtotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
                  </td>
                  <td>
                    <button onClick={() => handleRemove(item.id)}>Remover</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="cart-summary">
            <p className="cart-total">
              Total: {cart.total.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })}
            </p>
            <div className="cart-actions">
              <button onClick={handleClearCart}>Esvaziar carrinho</button>
              <button onClick={handleCheckout} disabled={submitting}>
                {submitting ? 'Finalizando...' : 'Finalizar pedido'}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
