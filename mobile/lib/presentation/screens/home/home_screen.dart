import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/l10n/app_strings.dart';
import 'package:futbolin/core/theme/app_theme.dart';
import 'package:futbolin/presentation/providers/locale_provider.dart';
import 'package:futbolin/presentation/providers/session_provider.dart';
import 'package:go_router/go_router.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(ref.watch(localeProvider));
    final session = ref.watch(sessionProvider);
    return Scaffold(
      body: session.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('$e')),
        data: (user) {
          if (user == null) return const SizedBox.shrink();
          return SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(children: [
                Row(children: [
                  CircleAvatar(radius: 28, backgroundColor: AppTheme.gold, child: Text(user.displayName.isEmpty ? '?' : user.displayName.substring(0, 1).toUpperCase(), style: const TextStyle(color: Colors.black, fontWeight: FontWeight.bold))),
                  const SizedBox(width: 12),
                  Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Text(user.displayName, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                    Text('${user.division} · Nv. ${user.level} · ${user.rankingPoints} pts', style: const TextStyle(color: Colors.white70)),
                  ])),
                  Column(children: [
                    Text('🪙 ${user.coins}', style: const TextStyle(color: AppTheme.gold, fontWeight: FontWeight.bold)),
                    Text('🔥 ${user.dailyStreak}', style: const TextStyle(color: Colors.orangeAccent)),
                  ]),
                ]),
                const SizedBox(height: 16),
                LinearProgressIndicator(value: (user.xp % 200) / 200, color: AppTheme.gold, backgroundColor: Colors.white10),
                const Spacer(),
                SizedBox(
                  width: double.infinity,
                  height: 64,
                  child: FilledButton(
                    style: FilledButton.styleFrom(backgroundColor: AppTheme.gold, foregroundColor: Colors.black),
                    onPressed: () => context.push('/queue'),
                    child: Text(s.playNow, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900, letterSpacing: 1.2)),
                  ),
                ),
                const SizedBox(height: 18),
                Wrap(spacing: 10, runSpacing: 10, children: [
                  _chip(context, Icons.lock, s.privateMatch, '/private'),
                  _chip(context, Icons.leaderboard, s.ranking, '/ranking'),
                  _chip(context, Icons.flag, s.missions, '/missions'),
                  _chip(context, Icons.person, s.profile, '/profile'),
                  _chip(context, Icons.store, s.store, '/store'),
                  _chip(context, Icons.bar_chart, 'Stats', '/stats'),
                  _chip(context, Icons.today, s.dailyQuestion, '/daily'),
                  _chip(context, Icons.bolt, s.survival, '/survival'),
                  _chip(context, Icons.history, s.history, '/history'),
                  _chip(context, Icons.group, s.friends, '/friends'),
                  _chip(context, Icons.emoji_events, s.tournaments, '/tournaments'),
                  _chip(context, Icons.whatshot, s.rivalries, '/rivalries'),
                ]),
                const Spacer(),
              ]),
            ),
          );
        },
      ),
    );
  }

  Widget _chip(BuildContext context, IconData icon, String label, String path) {
    return ActionChip(
      avatar: Icon(icon, size: 18, color: AppTheme.gold),
      label: Text(label),
      onPressed: () => context.push(path),
      backgroundColor: AppTheme.card,
    );
  }
}
