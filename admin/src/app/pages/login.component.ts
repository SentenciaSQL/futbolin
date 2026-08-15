import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="min-h-screen grid place-items-center bg-navy">
      <form class="bg-slate-900 p-8 rounded-2xl w-full max-w-sm space-y-4 border border-white/10" (ngSubmit)="submit()">
        <h1 class="text-2xl font-black text-gold">Futbolín Admin</h1>
        <input class="w-full p-3 rounded bg-black/40" [(ngModel)]="login" name="login" placeholder="admin@futbolin.app" />
        <input class="w-full p-3 rounded bg-black/40" [(ngModel)]="password" name="password" type="password" placeholder="Admin123!" />
        <button class="w-full bg-gold text-black font-bold py-3 rounded">Entrar</button>
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
