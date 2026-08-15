import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="text-3xl font-black mb-6">Temporadas</h1>
    <form class="card grid grid-cols-2 gap-3 mb-6" (ngSubmit)="create()">
      <input class="field" [(ngModel)]="draft.name" name="name" placeholder="Nombre" />
      <input class="field" [(ngModel)]="draft.slug" name="slug" placeholder="slug" />
      <input class="field" [(ngModel)]="draft.startsAt" name="starts" placeholder="Inicio ISO" />
      <input class="field" [(ngModel)]="draft.endsAt" name="ends" placeholder="Fin ISO" />
      <label class="flex items-center gap-2"><input type="checkbox" [(ngModel)]="draft.active" name="active" /> Activa</label>
      <button class="btn-gold">Crear</button>
    </form>
    <div class="space-y-2">
      @for (s of seasons; track s.id) {
        <div class="card">{{ s.name }} · {{ s.slug }} · {{ s.active ? 'activa' : 'cerrada' }}</div>
      }
    </div>
  `,
})
export class SeasonsComponent implements OnInit {
  seasons: any[] = [];
  draft = { name: '', slug: '', startsAt: new Date().toISOString(), endsAt: new Date(Date.now() + 90 * 86400000).toISOString(), active: false };
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.reload();
  }
  reload() {
    this.api.seasons().subscribe((p) => (this.seasons = p.content ?? p));
  }
  create() {
    this.api.createSeason(this.draft).subscribe(() => this.reload());
  }
}
