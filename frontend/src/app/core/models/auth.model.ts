export interface LoginRequest {
  email: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  rol: Rol;
  usuarioId: string;
  organizacionId: string;
}

export type Rol = 'CLIENTE' | 'GESTOR' | 'ADMINISTRADOR';

export interface DemoUser {
  email: string;
  label: string;
  rol: Rol;
}

export const DEMO_USERS: DemoUser[] = [
  { email: 'cliente@bancoa.demo', label: 'Cliente', rol: 'CLIENTE' },
  { email: 'gestor@bancoa.demo', label: 'Gestor', rol: 'GESTOR' },
  { email: 'admin@bancoa.demo', label: 'Administrador', rol: 'ADMINISTRADOR' },
];
