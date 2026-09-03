export interface CategoryResponse {
  id: number;
  name: string;
}

export interface ProductResponse {
  id: number;
  name: string;
  description: string | null;
  price: number;
  stockQty: number;
  category: CategoryResponse | null;
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // página atual (0-based)
  size: number;
  first: boolean;
  last: boolean;
}

export interface CartItemResponse {
  id: number;
  product: ProductResponse;
  quantity: number;
  subtotal: number;
}

export interface CartResponse {
  items: CartItemResponse[];
  total: number;
}

export interface OrderItemResponse {
  id: number;
  product: ProductResponse;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';

export interface OrderResponse {
  id: number;
  status: OrderStatus;
  total: number;
  items: OrderItemResponse[];
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthenticatedUser {
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  message?: string;
  errors?: Record<string, string>;
}
