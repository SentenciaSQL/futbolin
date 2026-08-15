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

  static Future<AppStrings> load(Locale locale) async {
    final code = locale.languageCode == 'en' ? 'en' : 'es';
    final raw = await rootBundle.loadString('assets/i18n/$code.json');
    final map = (jsonDecode(raw) as Map<String, dynamic>).map((k, v) => MapEntry(k, '$v'));
    return AppStrings(map);
  }

  static AppStrings of(Locale locale) => locale.languageCode == 'en' ? _en : _es;

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
  });
}
