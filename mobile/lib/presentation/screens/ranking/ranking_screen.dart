import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class RankingScreen extends ConsumerWidget {
  const RankingScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final future = ref.watch(dioProvider).get('/api/v1/rankings');
    return Scaffold(
      appBar: AppBar(title: const Text('Ranking global')),
      body: FutureBuilder(
        future: future,
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final content = (snap.data!.data['content'] as List?) ?? [];
          return ListView.builder(
            itemCount: content.length,
            itemBuilder: (_, i) {
              final row = content[i] as Map;
              return ListTile(
                leading: Text('#${i + 1}'),
                title: Text('${row['displayName'] ?? row['username'] ?? ''}'),
                subtitle: Text('${row['division'] ?? ''}'),
                trailing: Text('${row['rankingPoints'] ?? row['points'] ?? ''}'),
              );
            },
          );
        },
      ),
    );
  }
}
