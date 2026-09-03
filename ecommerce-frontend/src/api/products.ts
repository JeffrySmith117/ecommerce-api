import { apiClient } from './client';
import type { Page, ProductResponse, CategoryResponse } from '../types';

export interface FindProductsParams {
  page?: number;
  size?: number;
  search?: string;
  categoryId?: number;
}

export async function findProducts(params: FindProductsParams): Promise<Page<ProductResponse>> {
  const { data } = await apiClient.get<Page<ProductResponse>>('/products', {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 12,
      search: params.search || undefined,
      categoryId: params.categoryId || undefined,
    },
  });
  return data;
}

export async function findCategories(): Promise<CategoryResponse[]> {
  const { data } = await apiClient.get<CategoryResponse[]>('/categories');
  return data;
}
