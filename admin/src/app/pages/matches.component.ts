import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Partidas</h1>
    <div class="space-y-2">
      @for (m of matches; track m.id) {
        <div class="card">
          {{ m.id }} · {{ m.status }} · {{ m.scoreA }}-{{ m.scoreB }}
        </div>
      }
    </div>
  `,
})
export class MatchesComponent implements OnInit {
  matches: any[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.api.matches().subscribe((p) => (this.matches = p.content ?? p));
  }
}
