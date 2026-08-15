import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class StoreScreen extends ConsumerWidget {
  const StoreScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Tienda cosmética')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/store/cosmetics'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final items = snap.data!.data as List;
          return GridView.count(
            crossAxisCount: 2,
            padding: const EdgeInsets.all(12),
            children: items.map((item) {
              final m = item as Map;
              return Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
                    Text('${m['nameEs']}', textAlign: TextAlign.center),
                    Text('${m['type']} · ${m['rarity']}', style: const TextStyle(fontSize: 12)),
                    Text('${m['priceCoins']} 🪙'),
                    TextButton(
                      onPressed: () => ref.read(dioProvider).post('/api/v1/store/cosmetics/${m['id']}/buy'),
                      child: const Text('Comprar'),
                    ),
                  ]),
                ),
              );
            }).toList(),
          );
        },
      ),
    );
  }
}
