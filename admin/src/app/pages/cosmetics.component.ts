import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <h1 class="text-3xl font-black mb-6">Cosméticos</h1>
    <form class="grid grid-cols-2 gap-3 bg-slate-900 p-4 rounded-xl mb-6" (ngSubmit)="create()">
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.key" name="key" placeholder="ball_gold" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.type" name="type" placeholder="BALL" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.nameEs" name="nameEs" placeholder="Nombre ES" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.nameEn" name="nameEn" placeholder="Name EN" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.rarity" name="rarity" placeholder="RARE" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.priceCoins" name="price" type="number" placeholder="Precio" />
      <input class="p-2 bg-black/40 rounded" [(ngModel)]="draft.minLevel" name="level" type="number" placeholder="Nivel mín." />
      <button class="col-span-2 bg-gold text-black font-bold py-2 rounded">Crear</button>
    </form>
    <div class="space-y-2">
      @for (c of cosmetics; track c.id) {
        <div class="bg-slate-900 p-3 rounded">{{ c.key }} · {{ c.nameEs }} · {{ c.priceCoins }} 🪙</div>
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
