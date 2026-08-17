export interface LoginRequest {
  email: string;
  password: string;
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
  password: string;
  label: string;
  rol: Rol;
}

export const DEMO_PASSWORD = 'demo';

export const DEMO_USERS: DemoUser[] = [
  { email: 'cliente@bancoa.demo', password: DEMO_PASSWORD, label: 'Cliente', rol: 'CLIENTE' },
  { email: 'gestor@bancoa.demo', password: DEMO_PASSWORD, label: 'Gestor', rol: 'GESTOR' },
  { email: 'admin@bancoa.demo', password: DEMO_PASSWORD, label: 'Administrador', rol: 'ADMINISTRADOR' },
];
