import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="min-h-screen grid place-items-center bg-gradient-to-b from-navy via-pitch to-navy p-6">
      <form class="card w-full max-w-sm space-y-4" (ngSubmit)="submit()">
        <div class="text-center space-y-1">
          <p class="text-4xl">⚽</p>
          <h1 class="text-3xl font-black text-gold">Futbolín</h1>
          <p class="text-white/60 text-sm">Panel de dirección técnica</p>
        </div>
        <input class="field" [(ngModel)]="login" name="login" placeholder="admin@futbolin.app" />
        <input class="field" [(ngModel)]="password" name="password" type="password" placeholder="Admin123!" />
        <button class="btn-gold w-full py-3">Entrar</button>
        @if (error) {
          <p class="text-red-400 text-sm">{{ error }}</p>
        }
      </form>
    </div>
  `,
})
export class LoginComponent {
  login = 'admin@futbolin.app';
  password = 'Admin123!';
  error = '';
  constructor(private api: ApiService, private router: Router) {}
  submit() {
    this.api.login(this.login, this.password).subscribe({
      next: (res) => {
        localStorage.setItem('admin_token', res.accessToken);
        this.api.token = res.accessToken;
        this.router.navigateByUrl('/');
      },
      error: () => (this.error = 'Credenciales inválidas'),
    });
  }
}
