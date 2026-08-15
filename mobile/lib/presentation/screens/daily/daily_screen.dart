import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class DailyScreen extends ConsumerStatefulWidget {
  const DailyScreen({super.key});
  @override
  ConsumerState<DailyScreen> createState() => _DailyScreenState();
}

class _DailyScreenState extends ConsumerState<DailyScreen> {
  Map<String, dynamic>? result;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Pregunta del día')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/daily-challenge'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final q = snap.data!.data['question'] ?? snap.data!.data;
          return Padding(
            padding: const EdgeInsets.all(16),
            child: Column(children: [
              Text('${q['promptEs'] ?? q}', style: const TextStyle(fontSize: 20)),
              const SizedBox(height: 16),
              if (result != null) Text('Global: ${result!['globalCorrectPercent']}% acertó'),
            ]),
          );
        },
      ),
    );
  }
}
