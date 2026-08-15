import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="text-3xl font-black mb-6">Misiones</h1>
    <form class="card grid grid-cols-2 gap-3 mb-6" (ngSubmit)="create()">
      <input class="field" [(ngModel)]="draft.code" name="code" placeholder="WIN_3" />
      <input class="field" [(ngModel)]="draft.period" name="period" placeholder="DAILY" />
      <input class="field" [(ngModel)]="draft.nameEs" name="nameEs" placeholder="Nombre ES" />
      <input class="field" [(ngModel)]="draft.nameEn" name="nameEn" placeholder="Name EN" />
      <input class="field" [(ngModel)]="draft.descriptionEs" name="descriptionEs" placeholder="Descripción ES" />
      <input class="field" [(ngModel)]="draft.metric" name="metric" placeholder="WINS" />
      <input class="field" [(ngModel)]="draft.target" name="target" type="number" placeholder="3" />
      <input class="field" [(ngModel)]="draft.xpReward" name="xp" type="number" placeholder="XP" />
      <input class="field" [(ngModel)]="draft.coinsReward" name="coins" type="number" placeholder="Coins" />
      <button class="btn-gold col-span-2 py-2">Crear</button>
    </form>
    <div class="space-y-2">
      @for (m of missions; track m.id) {
        <div class="card">{{ m.code }} · {{ m.nameEs }} · {{ m.target }}</div>
      }
    </div>
  `,
})
export class MissionsComponent implements OnInit {
  missions: any[] = [];
  draft = { code: '', period: 'DAILY', nameEs: '', nameEn: '', descriptionEs: '', descriptionEn: '', metric: 'WINS', target: 3, xpReward: 50, coinsReward: 20, active: true };
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.reload();
  }
  reload() {
    this.api.missions().subscribe((p) => (this.missions = p.content ?? p));
  }
  create() {
    this.api.createMission({ ...this.draft, descriptionEn: this.draft.descriptionEs }).subscribe(() => this.reload());
  }
}
