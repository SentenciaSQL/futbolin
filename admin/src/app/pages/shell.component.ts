import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen grid grid-cols-[240px_1fr]">
      <aside class="bg-pitch p-6 space-y-3">
        <h2 class="font-black text-gold text-xl mb-6">⚽ Futbolín</h2>
        <a routerLink="/" routerLinkActive="text-gold" [routerLinkActiveOptions]="{exact:true}" class="block">Dashboard</a>
        <a routerLink="/questions" routerLinkActive="text-gold" class="block">Preguntas</a>
        <a routerLink="/reports" routerLinkActive="text-gold" class="block">Reportes</a>
        <a routerLink="/users" routerLinkActive="text-gold" class="block">Usuarios</a>
        <a routerLink="/matches" routerLinkActive="text-gold" class="block">Partidas</a>
        <a routerLink="/seasons" routerLinkActive="text-gold" class="block">Temporadas</a>
        <a routerLink="/missions" routerLinkActive="text-gold" class="block">Misiones</a>
        <a routerLink="/cosmetics" routerLinkActive="text-gold" class="block">Cosméticos</a>
        <a routerLink="/tournaments" routerLinkActive="text-gold" class="block">Torneos</a>
      </aside>
      <main class="p-8 overflow-auto"><router-outlet /></main>
    </div>
  `,
})
export class ShellComponent {}
