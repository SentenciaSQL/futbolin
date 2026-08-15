import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="text-3xl font-black mb-6">Torneos 16</h1>
    <form class="card grid grid-cols-2 gap-3 mb-6" (ngSubmit)="create()">
      <input class="field" [(ngModel)]="name" name="name" placeholder="Copa de fin de semana" />
      <input class="field" [(ngModel)]="theme" name="theme" placeholder="WEEKEND" />
      <button class="btn-gold col-span-2 py-2">Crear copa</button>
    </form>
    <div class="space-y-2">
      @for (t of tournaments; track t.id) {
        <div class="card">{{ t.name }} · {{ t.status }} · {{ t.size }} jugadores</div>
      }
    </div>
  `,
})
export class TournamentsComponent implements OnInit {
  tournaments: any[] = [];
  name = 'Copa Futbolín';
  theme = 'WEEKEND';
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.reload();
  }
  reload() {
    this.api.tournaments().subscribe((p) => (this.tournaments = p.content ?? p));
  }
  create() {
    this.api.createTournament({ name: this.name, theme: this.theme }).subscribe(() => this.reload());
  }
}
