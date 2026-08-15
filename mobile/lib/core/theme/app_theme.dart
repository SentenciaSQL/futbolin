import 'package:flutter/material.dart';

class AppTheme {
  static const pitchGreen = Color(0xFF0B3D2E);
  static const grass = Color(0xFF147A4B);
  static const gold = Color(0xFFE3B341);
  static const navy = Color(0xFF071422);
  static const card = Color(0xFF102033);

  static ThemeData dark() {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: navy,
      colorScheme: const ColorScheme.dark(
        primary: gold,
        secondary: grass,
        surface: card,
      ),
      useMaterial3: true,
      appBarTheme: const AppBarTheme(backgroundColor: Colors.transparent, elevation: 0),
    );
  }
}
