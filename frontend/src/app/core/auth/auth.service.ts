import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse, Rol } from '../models/auth.model';

const SESSION_KEY = 'helpdesk_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = signal<LoginResponse | null>(this.loadSession());

  readonly currentUser = computed(() => this.session());
  readonly isAuthenticated = computed(() => !!this.session()?.token);
  readonly rol = computed(() => this.session()?.rol ?? null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly usuarioId = computed(() => this.session()?.usuarioId ?? null);
  readonly organizacionId = computed(() => this.session()?.organizacionId ?? null);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => this.persistSession(response))
    );
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(SESSION_KEY);
  }

  hasRole(...roles: Rol[]): boolean {
    const rol = this.rol();
    return !!rol && roles.includes(rol);
  }

  private persistSession(response: LoginResponse): void {
    if (!this.isSessionComplete(response)) {
      this.logout();
      return;
    }
    this.session.set(response);
    localStorage.setItem(SESSION_KEY, JSON.stringify(response));
  }

  private loadSession(): LoginResponse | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as LoginResponse;
      if (!this.isSessionComplete(parsed)) {
        localStorage.removeItem(SESSION_KEY);
        return null;
      }
      return parsed;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private isSessionComplete(session: LoginResponse | null): boolean {
    return !!session?.token && !!session.usuarioId && !!session.organizacionId;
  }
}
