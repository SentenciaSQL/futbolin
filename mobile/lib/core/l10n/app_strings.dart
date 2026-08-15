import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class AppStrings {
  AppStrings(this._values);
  final Map<String, String> _values;

  String get appName => _values['appName'] ?? 'Futbolín';
  String get playNow => _values['playNow'] ?? 'JUGAR AHORA';
  String get privateMatch => _values['privateMatch'] ?? 'Partida privada';
  String get ranking => _values['ranking'] ?? 'Ranking';
  String get missions => _values['missions'] ?? 'Misiones';
  String get profile => _values['profile'] ?? 'Perfil';
  String get store => _values['store'] ?? 'Tienda';
  String get season => _values['season'] ?? 'Temporada';
  String get login => _values['login'] ?? 'Iniciar sesión';
  String get register => _values['register'] ?? 'Crear cuenta';
  String get rematch => _values['rematch'] ?? 'REVANCHA';
  String get findAnother => _values['findAnother'] ?? 'BUSCAR OTRO RIVAL';
  String get searchingRival => _values['searchingRival'] ?? 'Buscando rival...';
  String get goal => _values['goal'] ?? '¡GOOOOOOL!';
  String get dailyQuestion => _values['dailyQuestion'] ?? 'Pregunta del día';
  String get survival => _values['survival'] ?? 'Supervivencia';
  String get reconnecting => _values['reconnecting'] ?? 'Reconectando...';
  String get history => _values['history'] ?? 'Historial';
  String get friends => _values['friends'] ?? 'Amigos';
  String get tournaments => _values['tournaments'] ?? 'Torneos';
  String get rivalries => _values['rivalries'] ?? 'Rivalidades';

  static const supported = ['es', 'en', 'pt', 'fr', 'it', 'de'];

  static Future<AppStrings> load(Locale locale) async {
    final code = supported.contains(locale.languageCode) ? locale.languageCode : 'es';
    final raw = await rootBundle.loadString('assets/i18n/$code.json');
    final map = (jsonDecode(raw) as Map<String, dynamic>).map((k, v) => MapEntry(k, '$v'));
    return AppStrings(map);
  }

  static AppStrings of(Locale locale) {
    final code = locale.languageCode;
    return switch (code) {
      'en' => _en,
      'pt' => _pt,
      'fr' => _fr,
      'it' => _it,
      'de' => _de,
      _ => _es,
    };
  }

  static final _es = AppStrings({
    'appName': 'Futbolín',
    'playNow': 'JUGAR AHORA',
    'privateMatch': 'Partida privada',
    'ranking': 'Ranking',
    'missions': 'Misiones',
    'profile': 'Perfil',
    'store': 'Tienda',
    'season': 'Temporada',
    'login': 'Iniciar sesión',
    'register': 'Crear cuenta',
    'rematch': 'REVANCHA',
    'findAnother': 'BUSCAR OTRO RIVAL',
    'searchingRival': 'Buscando rival...',
    'goal': '¡GOOOOOOL!',
    'dailyQuestion': 'Pregunta del día',
    'survival': 'Supervivencia',
    'reconnecting': 'Reconectando...',
    'history': 'Historial',
    'friends': 'Amigos',
    'tournaments': 'Torneos',
    'rivalries': 'Rivalidades',
  });

  static final _en = AppStrings({
    'appName': 'Futbolin',
    'playNow': 'PLAY NOW',
    'privateMatch': 'Private match',
    'ranking': 'Ranking',
    'missions': 'Missions',
    'profile': 'Profile',
    'store': 'Store',
    'season': 'Season',
    'login': 'Sign in',
    'register': 'Create account',
    'rematch': 'REMATCH',
    'findAnother': 'FIND ANOTHER RIVAL',
    'searchingRival': 'Finding opponent...',
    'goal': 'GOOOOOAL!',
    'dailyQuestion': 'Question of the day',
    'survival': 'Survival',
    'reconnecting': 'Reconnecting...',
    'history': 'History',
    'friends': 'Friends',
    'tournaments': 'Tournaments',
    'rivalries': 'Rivalries',
  });

  static final _pt = AppStrings({
    'appName': 'Futbolín',
    'playNow': 'JOGAR AGORA',
    'privateMatch': 'Partida privada',
    'ranking': 'Ranking',
    'missions': 'Missões',
    'profile': 'Perfil',
    'store': 'Loja',
    'season': 'Temporada',
    'login': 'Entrar',
    'register': 'Criar conta',
    'rematch': 'REVANCHE',
    'findAnother': 'BUSCAR OUTRO RIVAL',
    'searchingRival': 'Procurando rival...',
    'goal': 'GOOOOOOL!',
    'dailyQuestion': 'Pergunta do dia',
    'survival': 'Sobrevivência',
    'reconnecting': 'Reconectando...',
    'history': 'Histórico',
    'friends': 'Amigos',
    'tournaments': 'Torneios',
    'rivalries': 'Rivalidades',
  });

  static final _fr = AppStrings({
    'appName': 'Futbolín',
    'playNow': 'JOUER',
    'privateMatch': 'Match privé',
    'ranking': 'Classement',
    'missions': 'Missions',
    'profile': 'Profil',
    'store': 'Boutique',
    'season': 'Saison',
    'login': 'Connexion',
    'register': 'Créer un compte',
    'rematch': 'REVANCHE',
    'findAnother': 'AUTRE ADVERSAIRE',
    'searchingRival': 'Recherche d’adversaire...',
    'goal': 'BUUUUUT !',
    'dailyQuestion': 'Question du jour',
    'survival': 'Survie',
    'reconnecting': 'Reconnexion...',
    'history': 'Historique',
    'friends': 'Amis',
    'tournaments': 'Tournois',
    'rivalries': 'Rivalités',
  });

  static final _it = AppStrings({
    'appName': 'Futbolín',
    'playNow': 'GIOCA ORA',
    'privateMatch': 'Partita privata',
    'ranking': 'Classifica',
    'missions': 'Missioni',
    'profile': 'Profilo',
    'store': 'Negozio',
    'season': 'Stagione',
    'login': 'Accedi',
    'register': 'Crea account',
    'rematch': 'RIVINCITA',
    'findAnother': 'CERCA UN ALTRO RIVALE',
    'searchingRival': 'Cercando avversario...',
    'goal': 'GOOOOOOL!',
    'dailyQuestion': 'Domanda del giorno',
    'survival': 'Sopravvivenza',
    'reconnecting': 'Riconnessione...',
    'history': 'Storico',
    'friends': 'Amici',
    'tournaments': 'Tornei',
    'rivalries': 'Rivalità',
  });

  static final _de = AppStrings({
    'appName': 'Futbolín',
    'playNow': 'JETZT SPIELEN',
    'privateMatch': 'Privates Spiel',
    'ranking': 'Rangliste',
    'missions': 'Missionen',
    'profile': 'Profil',
    'store': 'Shop',
    'season': 'Saison',
    'login': 'Anmelden',
    'register': 'Konto erstellen',
    'rematch': 'REVANCHE',
    'findAnother': 'ANDEREN GEGNER SUCHEN',
    'searchingRival': 'Gegner wird gesucht...',
    'goal': 'TOOOOOOR!',
    'dailyQuestion': 'Frage des Tages',
    'survival': 'Überleben',
    'reconnecting': 'Verbindung wird hergestellt...',
    'history': 'Verlauf',
    'friends': 'Freunde',
    'tournaments': 'Turniere',
    'rivalries': 'Rivalitäten',
  });
}
