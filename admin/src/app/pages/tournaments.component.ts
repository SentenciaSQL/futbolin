import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-black">Torneos 16</h1>
    </div>
    <form class="grid grid-cols-2 gap-3 bg-slate-900 p-4 rounded-xl mb-6" (ngSubmit)="create()">
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="name" name="name" placeholder="Copa de fin de semana" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="theme" name="theme" placeholder="WEEKEND" />
      <button class="col-span-2 bg-gold text-black font-bold py-2 rounded">Crear copa</button>
    </form>
    <div class="space-y-2">
      @for (t of tournaments; track t.id) {
        <div class="bg-slate-900 p-3 rounded">{{ t.name }} · {{ t.status }} · {{ t.size }} jugadores</div>
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
