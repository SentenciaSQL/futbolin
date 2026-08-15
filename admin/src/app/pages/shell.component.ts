import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="min-h-screen grid grid-cols-[240px_1fr] bg-navy">
      <aside class="bg-pitch p-6 space-y-1 border-r border-white/10">
        <h2 class="font-black text-gold text-xl mb-6">⚽ Futbolín</h2>
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}" class="nav-link">Dashboard</a>
        <a routerLink="/questions" routerLinkActive="active" class="nav-link">Preguntas</a>
        <a routerLink="/reports" routerLinkActive="active" class="nav-link">Reportes</a>
        <a routerLink="/users" routerLinkActive="active" class="nav-link">Usuarios</a>
        <a routerLink="/matches" routerLinkActive="active" class="nav-link">Partidas</a>
        <a routerLink="/seasons" routerLinkActive="active" class="nav-link">Temporadas</a>
        <a routerLink="/missions" routerLinkActive="active" class="nav-link">Misiones</a>
        <a routerLink="/cosmetics" routerLinkActive="active" class="nav-link">Cosméticos</a>
        <a routerLink="/tournaments" routerLinkActive="active" class="nav-link">Torneos</a>
      </aside>
      <main class="p-8 overflow-auto"><router-outlet /></main>
    </div>
  `,
})
export class ShellComponent {}
