import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/presentation/providers/session_provider.dart';
import 'package:go_router/go_router.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(sessionProvider).asData?.value;
    return Scaffold(
      appBar: AppBar(title: const Text('Perfil')),
      body: user == null
          ? const SizedBox.shrink()
          : ListView(padding: const EdgeInsets.all(20), children: [
              Text(user.displayName, style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
              Text('@${user.username}'),
              Text('${user.country ?? ''} · ${user.favoriteTeam ?? ''}'),
              Text('División ${user.division} · ${user.rankingPoints} pts'),
              const SizedBox(height: 24),
              FilledButton(onPressed: () => context.push('/history'), child: const Text('Historial')),
              const SizedBox(height: 8),
              OutlinedButton(onPressed: () => context.push('/rivalries'), child: const Text('Rivalidades')),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: () async {
                  await ref.read(authRepositoryProvider).logout();
                  ref.invalidate(sessionProvider);
                  if (context.mounted) context.go('/auth');
                },
                child: const Text('Cerrar sesión'),
              ),
            ]),
    );
  }
}
