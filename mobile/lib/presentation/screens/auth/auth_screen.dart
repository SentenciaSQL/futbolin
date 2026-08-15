import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/l10n/app_strings.dart';
import 'package:futbolin/core/theme/app_theme.dart';
import 'package:futbolin/presentation/providers/locale_provider.dart';
import 'package:futbolin/presentation/providers/session_provider.dart';
import 'package:go_router/go_router.dart';

class AuthScreen extends ConsumerStatefulWidget {
  const AuthScreen({super.key});
  @override
  ConsumerState<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends ConsumerState<AuthScreen> {
  final email = TextEditingController();
  final username = TextEditingController();
  final password = TextEditingController();
  bool register = false;
  String? error;
  bool loading = false;

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(ref.watch(localeProvider));
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [AppTheme.navy, AppTheme.pitchGreen],
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
          ),
        ),
        padding: const EdgeInsets.all(24),
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: Column(mainAxisSize: MainAxisSize.min, children: [
              const Text('⚽', style: TextStyle(fontSize: 56)),
              Text(s.appName, style: const TextStyle(fontSize: 36, fontWeight: FontWeight.w800, color: AppTheme.gold)),
              const SizedBox(height: 8),
              const Text('Trivia + partido en tiempo real', style: TextStyle(color: Colors.white70)),
              const SizedBox(height: 28),
              if (register) _field(username, s.register.contains('cuenta') ? 'Usuario' : 'Username'),
              _field(email, 'Email'),
              _field(password, 'Password', obscure: true),
              if (error != null) Padding(padding: const EdgeInsets.only(top: 8), child: Text(error!, style: const TextStyle(color: Colors.redAccent))),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                height: 52,
                child: FilledButton(
                  onPressed: loading ? null : _submit,
                  child: Text(register ? s.register : s.login, style: const TextStyle(fontWeight: FontWeight.bold)),
                ),
              ),
              TextButton(
                onPressed: () => setState(() => register = !register),
                child: Text(register ? s.login : s.register),
              ),
            ]),
          ),
        ),
      ),
    );
  }

  Widget _field(TextEditingController c, String hint, {bool obscure = false}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: TextField(
        controller: c,
        obscureText: obscure,
        decoration: InputDecoration(hintText: hint, filled: true, fillColor: Colors.black26, border: OutlineInputBorder(borderRadius: BorderRadius.circular(14))),
      ),
    );
  }

  Future<void> _submit() async {
    setState(() { loading = true; error = null; });
    try {
      final repo = ref.read(authRepositoryProvider);
      if (register) {
        await repo.register(email: email.text, username: username.text, password: password.text);
      } else {
        await repo.login(email.text, password.text);
      }
      ref.invalidate(sessionProvider);
      if (mounted) context.go('/home');
    } catch (e) {
      setState(() => error = e.toString());
    } finally {
      if (mounted) setState(() => loading = false);
    }
  }
}
