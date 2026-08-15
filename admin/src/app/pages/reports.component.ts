import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Reportes de preguntas</h1>
    <div class="space-y-2">
      @for (r of reports; track r.id) {
        <div class="bg-slate-900 p-3 rounded flex justify-between">
          <div>{{ r.reason }} · {{ r.details }}</div>
          <button class="bg-gold text-black px-3 py-1 rounded" (click)="resolve(r)">Resolver</button>
        </div>
      }
    </div>
  `,
})
export class ReportsComponent implements OnInit {
  reports: any[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.api.reports().subscribe((r) => (this.reports = r));
  }
  resolve(r: any) {
    this.api.resolve(r.id).subscribe(() => (this.reports = this.reports.filter((x) => x.id !== r.id)));
  }
}
