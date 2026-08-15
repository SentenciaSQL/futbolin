import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class MissionsScreen extends ConsumerWidget {
  const MissionsScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Misiones y logros')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/missions'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final catalog = (snap.data!.data['catalog'] as List?) ?? [];
          return ListView(
            children: catalog.map((m) {
              final map = m as Map;
              return ListTile(
                title: Text('${map['nameEs'] ?? map['code']}'),
                subtitle: Text('${map['descriptionEs'] ?? ''} · ${map['target']}'),
                trailing: Text('+${map['coinsReward']} 🪙'),
              );
            }).toList(),
          );
        },
      ),
    );
  }
}
