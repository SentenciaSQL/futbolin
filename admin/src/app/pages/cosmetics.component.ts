import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="text-3xl font-black mb-6">Cosméticos</h1>
    <form class="card grid grid-cols-2 gap-3 mb-6" (ngSubmit)="create()">
      <input class="field" [(ngModel)]="draft.key" name="key" placeholder="ball_gold" />
      <input class="field" [(ngModel)]="draft.type" name="type" placeholder="BALL" />
      <input class="field" [(ngModel)]="draft.nameEs" name="nameEs" placeholder="Nombre ES" />
      <input class="field" [(ngModel)]="draft.nameEn" name="nameEn" placeholder="Name EN" />
      <input class="field" [(ngModel)]="draft.rarity" name="rarity" placeholder="RARE" />
      <input class="field" [(ngModel)]="draft.priceCoins" name="price" type="number" placeholder="Precio" />
      <input class="field" [(ngModel)]="draft.minLevel" name="level" type="number" placeholder="Nivel mín." />
      <button class="btn-gold col-span-2 py-2">Crear</button>
    </form>
    <div class="space-y-2">
      @for (c of cosmetics; track c.id) {
        <div class="card">{{ c.key }} · {{ c.nameEs }} · {{ c.priceCoins }} 🪙</div>
      }
    </div>
  `,
})
export class CosmeticsComponent implements OnInit {
  cosmetics: any[] = [];
  draft = { key: '', type: 'BALL', nameEs: '', nameEn: '', rarity: 'COMMON', priceCoins: 100, minLevel: 1, active: true };
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.reload();
  }
  reload() {
    this.api.cosmetics().subscribe((p) => (this.cosmetics = p.content ?? p));
  }
  create() {
    this.api.createCosmetic(this.draft).subscribe(() => this.reload());
  }
}
