import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class PublicProfileScreen extends ConsumerWidget {
  const PublicProfileScreen({super.key, required this.userId});
  final String userId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Perfil público')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/users/$userId'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final row = Map<String, dynamic>.from(snap.data!.data as Map);
          return ListView(padding: const EdgeInsets.all(20), children: [
            Text('${row['displayName']}', style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
            Text('@${row['username']}'),
            Text('${row['country'] ?? ''} · ${row['favoriteTeam'] ?? ''}'),
            Text('${row['division']} · ${row['rankingPoints']} pts'),
            Text('W ${row['wins']} / L ${row['losses']} / D ${row['draws']}'),
            Text('Goles ${row['goalsScored']}-${row['goalsConceded']} · Precisión ${row['accuracy']}'),
          ]),
        },
      ),
    );
  }
}
