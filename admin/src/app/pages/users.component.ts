import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Usuarios</h1>
    <div class="space-y-2">
      @for (u of users; track u.id) {
        <div class="card flex justify-between items-center">
          <div>
            <div class="font-bold">{{ u.username }}</div>
            <div class="text-white/50 text-sm">{{ u.email }}</div>
          </div>
          <button class="rounded bg-red-700 px-3 py-1 text-sm font-semibold hover:bg-red-600" (click)="lock(u)">
            {{ u.locked ? 'Desbloquear' : 'Bloquear' }}
          </button>
        </div>
      }
    </div>
  `,
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.api.users().subscribe((p) => (this.users = p.content ?? p));
  }
  lock(u: any) {
    this.api.lock(u.id, !u.locked).subscribe(() => (u.locked = !u.locked));
  }
}
