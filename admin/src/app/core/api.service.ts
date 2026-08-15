import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiService {
  base = '/api/v1';
  token = localStorage.getItem('admin_token') ?? '';

  constructor(private http: HttpClient) {}

  headers() {
    return { Authorization: `Bearer ${this.token}` };
  }

  login(login: string, password: string) {
    return this.http.post<any>(`${this.base}/auth/login`, { login, password });
  }

  dashboard() {
    return this.http.get<any>(`${this.base}/admin/dashboard`, { headers: this.headers() });
  }

  questions(page = 0) {
    return this.http.get<any>(`${this.base}/admin/questions?page=${page}`, { headers: this.headers() });
  }

  createQuestion(body: unknown) {
    return this.http.post(`${this.base}/admin/questions`, body, { headers: this.headers() });
  }

  importFile(file: File) {
    const data = new FormData();
    data.append('file', file);
    return this.http.post(`${this.base}/admin/questions/import`, data, { headers: this.headers() });
  }

  users(page = 0) {
    return this.http.get<any>(`${this.base}/admin/users?page=${page}`, { headers: this.headers() });
  }

  lock(id: string, locked: boolean) {
    return this.http.post(`${this.base}/admin/users/${id}/lock?locked=${locked}`, {}, { headers: this.headers() });
  }

  reports() {
    return this.http.get<any>(`${this.base}/admin/reports`, { headers: this.headers() });
  }

  resolve(id: string) {
    return this.http.post(`${this.base}/admin/reports/${id}/resolve`, { status: 'RESOLVED' }, { headers: this.headers() });
  }

  matches(page = 0) {
    return this.http.get<any>(`${this.base}/admin/matches?page=${page}`, { headers: this.headers() });
  }

  seasons() {
    return this.http.get<any>(`${this.base}/admin/seasons`, { headers: this.headers() });
  }

  createSeason(body: unknown) {
    return this.http.post(`${this.base}/admin/seasons`, body, { headers: this.headers() });
  }

  missions() {
    return this.http.get<any>(`${this.base}/admin/missions`, { headers: this.headers() });
  }

  createMission(body: unknown) {
    return this.http.post(`${this.base}/admin/missions`, body, { headers: this.headers() });
  }

  cosmetics() {
    return this.http.get<any>(`${this.base}/admin/cosmetics`, { headers: this.headers() });
  }

  createCosmetic(body: unknown) {
    return this.http.post(`${this.base}/admin/cosmetics`, body, { headers: this.headers() });
  }

  tournaments() {
    return this.http.get<any>(`${this.base}/admin/tournaments`, { headers: this.headers() });
  }

  createTournament(body: unknown) {
    return this.http.post(`${this.base}/admin/tournaments`, body, { headers: this.headers() });
  }
}
