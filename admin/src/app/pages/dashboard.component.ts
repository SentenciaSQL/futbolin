import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Panel</h1>
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      @for (k of keys; track k) {
        <div class="card">
          <div class="text-white/60 text-sm uppercase tracking-wide">{{ labels[k] || k }}</div>
          <div class="text-3xl font-black text-gold mt-1">{{ data[k] }}</div>
        </div>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  data: Record<string, number> = {};
  keys: string[] = [];
  labels: Record<string, string> = {
    users: 'Usuarios',
    questions: 'Preguntas',
    liveMatches: 'Partidas en vivo',
    openReports: 'Reportes abiertos',
  };
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.api.dashboard().subscribe((d) => {
      this.data = d;
      this.keys = Object.keys(d);
    });
  }
}
