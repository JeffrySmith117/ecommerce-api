import { apiClient } from './client';
import type { OrderResponse, Page } from '../types';

export async function createOrder(): Promise<OrderResponse> {
  const { data } = await apiClient.post<OrderResponse>('/orders');
  return data;
}

export async function findMyOrders(page = 0, size = 10): Promise<Page<OrderResponse>> {
  const { data } = await apiClient.get<Page<OrderResponse>>('/orders', {
    params: { page, size },
  });
  return data;
}

export async function findOrderById(id: number): Promise<OrderResponse> {
  const { data } = await apiClient.get<OrderResponse>(`/orders/${id}`);
  return data;
}
