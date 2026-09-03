import { apiClient } from './client';
import type { CartResponse } from '../types';

export async function getCart(): Promise<CartResponse> {
  const { data } = await apiClient.get<CartResponse>('/cart');
  return data;
}

export async function addItem(productId: number, quantity: number): Promise<CartResponse> {
  const { data } = await apiClient.post<CartResponse>('/cart/items', { productId, quantity });
  return data;
}

export async function updateItem(
  itemId: number,
  productId: number,
  quantity: number,
): Promise<CartResponse> {
  const { data } = await apiClient.put<CartResponse>(`/cart/items/${itemId}`, {
    productId,
    quantity,
  });
  return data;
}

export async function removeItem(itemId: number): Promise<void> {
  await apiClient.delete(`/cart/items/${itemId}`);
}

export async function clearCart(): Promise<void> {
  await apiClient.delete('/cart');
}
