import { Component, OnInit } from '@angular/core';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  template: `
    <h1 class="text-3xl font-black mb-6">Panel</h1>
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
      @for (k of keys; track k) {
        <div class="bg-slate-900 p-5 rounded-xl">
          <div class="text-white/60 text-sm">{{ k }}</div>
          <div class="text-3xl font-black text-gold">{{ data[k] }}</div>
        </div>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  data: Record<string, number> = {};
  keys: string[] = [];
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.api.dashboard().subscribe((d) => {
      this.data = d;
      this.keys = Object.keys(d);
    });
  }
}
