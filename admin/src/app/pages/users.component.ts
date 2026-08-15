import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Usuarios</h1>
    <div class="space-y-2">
      @for (u of users; track u.id) {
        <div class="flex justify-between bg-slate-900 p-3 rounded">
          <div>{{ u.username }} · {{ u.email }}</div>
          <button class="text-sm bg-red-700 px-3 py-1 rounded" (click)="lock(u)">{{ u.locked ? 'Desbloquear' : 'Bloquear' }}</button>
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
