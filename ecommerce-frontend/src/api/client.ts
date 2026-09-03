import axios, { AxiosError } from 'axios';
import type { ApiErrorBody } from '../types';

const TOKEN_KEY = 'ecommerce_token';

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Extrai uma mensagem legível do formato de erro do GlobalExceptionHandler da API:
 * { message } para a maioria dos erros, ou { errors: { campo: mensagem } } para
 * falhas de validação (400 do MethodArgumentNotValidException).
 */
export function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const err = error as AxiosError<ApiErrorBody>;
    const body = err.response?.data;
    if (body?.message) return body.message;
    if (body?.errors) {
      return Object.values(body.errors).join(', ');
    }
    if (err.message) return err.message;
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}
