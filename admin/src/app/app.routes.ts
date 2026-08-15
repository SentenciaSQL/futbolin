import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login.component';
import { ShellComponent } from './pages/shell.component';
import { DashboardComponent } from './pages/dashboard.component';
import { QuestionsComponent } from './pages/questions.component';
import { UsersComponent } from './pages/users.component';
import { ReportsComponent } from './pages/reports.component';
import { MatchesComponent } from './pages/matches.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', component: DashboardComponent },
      { path: 'questions', component: QuestionsComponent },
      { path: 'users', component: UsersComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'matches', component: MatchesComponent },
    ],
  },
];
