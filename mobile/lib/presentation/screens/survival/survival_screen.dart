import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';

class SurvivalScreen extends ConsumerStatefulWidget {
  const SurvivalScreen({super.key});
  @override
  ConsumerState<SurvivalScreen> createState() => _SurvivalScreenState();
}

class _SurvivalScreenState extends ConsumerState<SurvivalScreen> {
  Map<String, dynamic>? data;

  @override
  void initState() {
    super.initState();
    Future.microtask(() async {
      final res = await ref.read(dioProvider).post('/api/v1/survival/start');
      setState(() => data = Map<String, dynamic>.from(res.data as Map));
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Supervivencia ${data?['score'] ?? 0}')),
      body: data == null
          ? const Center(child: CircularProgressIndicator())
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Text('${data!['question']?['promptEs'] ?? ''}', style: const TextStyle(fontSize: 20)),
                const SizedBox(height: 12),
                ...(((data!['question']?['options'] as List?) ?? []).map((o) {
                  final m = o as Map;
                  return Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: FilledButton.tonal(
                      onPressed: () async {
                        final res = await ref.read(dioProvider).post('/api/v1/survival/answer', data: {'optionKey': m['key']});
                        setState(() => data = Map<String, dynamic>.from(res.data as Map));
                      },
                      child: Text('${m['textEs']}'),
                    ),
                  );
                })),
                if (data!['alive'] == false) Text('Fin. Récord: ${data!['best']}'),
              ],
            ),
    );
  }
}
