import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-black">Preguntas</h1>
      <label class="btn-gold cursor-pointer">
        Importar CSV/JSON/Excel
        <input type="file" hidden (change)="import($event)" />
      </label>
    </div>
    <form class="card grid grid-cols-2 gap-3 mb-6" (ngSubmit)="create()">
      <input class="field" [(ngModel)]="draft.promptEs" name="promptEs" placeholder="Pregunta ES" />
      <input class="field" [(ngModel)]="draft.promptEn" name="promptEn" placeholder="Question EN" />
      <input class="field" [(ngModel)]="draft.optionA" name="a" placeholder="Opción A" />
      <input class="field" [(ngModel)]="draft.optionB" name="b" placeholder="Opción B" />
      <input class="field" [(ngModel)]="draft.optionC" name="c" placeholder="Opción C" />
      <input class="field" [(ngModel)]="draft.optionD" name="d" placeholder="Opción D" />
      <input class="field" [(ngModel)]="draft.correctAnswer" name="ok" placeholder="A" />
      <input class="field" [(ngModel)]="draft.categoryCode" name="cat" placeholder="WORLD_CUP" />
      <button class="btn-gold col-span-2 py-2">Crear</button>
    </form>
    @if (importMsg) {
      <p class="text-emerald-400 mb-2">{{ importMsg }}</p>
    }
    <div class="space-y-2">
      @for (q of questions; track q.id) {
        <div class="card flex justify-between gap-4">
          <span>{{ q.promptEs }}</span>
          <span class="text-gold text-sm font-bold">{{ q.difficulty }}</span>
        </div>
      }
    </div>
  `,
})
export class QuestionsComponent implements OnInit {
  questions: any[] = [];
  importMsg = '';
  draft = { promptEs: '', promptEn: '', optionA: '', optionB: '', optionC: '', optionD: '', correctAnswer: 'A', categoryCode: 'WORLD_CUP' };
  constructor(private api: ApiService) {}
  ngOnInit() {
    this.reload();
  }
  reload() {
    this.api.questions().subscribe((p) => (this.questions = p.content ?? p));
  }
  create() {
    this.api.createQuestion({
      categoryCode: this.draft.categoryCode,
      type: 'MULTIPLE_CHOICE',
      difficulty: 'MEDIUM',
      promptEs: this.draft.promptEs,
      promptEn: this.draft.promptEn || this.draft.promptEs,
      explanationEs: '',
      explanationEn: '',
      imageUrl: null,
      correctAnswer: this.draft.correctAnswer,
      options: [
        { key: 'A', textEs: this.draft.optionA, textEn: this.draft.optionA },
        { key: 'B', textEs: this.draft.optionB, textEn: this.draft.optionB },
        { key: 'C', textEs: this.draft.optionC, textEn: this.draft.optionC },
        { key: 'D', textEs: this.draft.optionD, textEn: this.draft.optionD },
      ],
    }).subscribe(() => this.reload());
  }
  import(ev: Event) {
    const file = (ev.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.api.importFile(file).subscribe((r: any) => {
      this.importMsg = `Importadas ${r.imported}, duplicadas ${r.duplicates}`;
      this.reload();
    });
  }
}
