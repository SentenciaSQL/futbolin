import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class RivalriesScreen extends ConsumerWidget {
  const RivalriesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Rivalidades')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/users/me/rivalries'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final list = (snap.data!.data as List?) ?? [];
          if (list.isEmpty) return const Center(child: Text('Todavía no hay clásicos'));
          return ListView.builder(
            itemCount: list.length,
            itemBuilder: (_, i) {
              final row = list[i] as Map;
              return ListTile(
                leading: const Icon(Icons.whatshot),
                title: Text('${row['winsA'] ?? 0} - ${row['winsB'] ?? 0}  (${row['draws'] ?? 0} emp.)'),
                subtitle: Text('${row['matchesPlayed'] ?? 0} partidos'),
                onTap: () {
                  final other = '${row['userBId'] ?? row['userAId'] ?? ''}';
                  if (other.isNotEmpty) context.push('/users/$other');
                },
              );
            },
          );
        },
      ),
    );
  }
}
