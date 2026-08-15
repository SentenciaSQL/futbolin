import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/l10n/app_strings.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:futbolin/core/theme/app_theme.dart';
import 'package:futbolin/presentation/providers/locale_provider.dart';
import 'package:go_router/go_router.dart';

class QueueScreen extends ConsumerStatefulWidget {
  const QueueScreen({super.key});
  @override
  ConsumerState<QueueScreen> createState() => _QueueScreenState();
}

class _QueueScreenState extends ConsumerState<QueueScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(_queue);
  }

  Future<void> _queue() async {
    try {
      await ref.read(dioProvider).post('/api/v1/matches/queue');
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(ref.watch(localeProvider));
    return Scaffold(
      appBar: AppBar(title: Text(s.searchingRival)),
      body: Center(
        child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
          const Icon(Icons.sports_soccer, size: 72, color: AppTheme.gold),
          const SizedBox(height: 16),
          Text(s.searchingRival, style: const TextStyle(fontSize: 20)),
          const SizedBox(height: 24),
          const CircularProgressIndicator(color: AppTheme.gold),
          const SizedBox(height: 32),
          OutlinedButton(onPressed: () => context.go('/home'), child: const Text('Cancelar')),
        ]),
      ),
    );
  }
}
