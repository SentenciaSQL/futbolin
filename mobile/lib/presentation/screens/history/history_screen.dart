import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class HistoryScreen extends ConsumerWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Historial')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/users/me/history'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final content = (snap.data!.data['content'] as List?) ?? [];
          if (content.isEmpty) return const Center(child: Text('Aún no hay partidos'));
          return ListView.builder(
            itemCount: content.length,
            itemBuilder: (_, i) {
              final row = content[i] as Map;
              final id = '${row['id'] ?? ''}';
              return ListTile(
                leading: const Icon(Icons.sports_soccer),
                title: Text('${row['mode'] ?? ''} · ${row['scoreA'] ?? 0}-${row['scoreB'] ?? 0}'),
                subtitle: Text('${row['status'] ?? ''} · ${row['endReason'] ?? ''}'),
                onTap: id.isEmpty ? null : () => context.push('/match/$id'),
              );
            },
          );
        },
      ),
    );
  }
}
