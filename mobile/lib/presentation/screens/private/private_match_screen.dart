import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class PrivateMatchScreen extends ConsumerStatefulWidget {
  const PrivateMatchScreen({super.key});
  @override
  ConsumerState<PrivateMatchScreen> createState() => _PrivateMatchScreenState();
}

class _PrivateMatchScreenState extends ConsumerState<PrivateMatchScreen> {
  final code = TextEditingController();
  String? created;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Partida privada')),
      body: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(children: [
          FilledButton(
            onPressed: () async {
              final res = await ref.read(dioProvider).post('/api/v1/matches/private');
              setState(() => created = res.data['code']?.toString());
            },
            child: const Text('Crear código FUT-XXXX'),
          ),
          if (created != null) Padding(padding: const EdgeInsets.all(12), child: Text(created!, style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold))),
          if (created != null) Text('Invitación: futbolin://join/$created', textAlign: TextAlign.center),
          const Divider(),
          TextField(controller: code, decoration: const InputDecoration(hintText: 'FUT-8392')),
          const SizedBox(height: 8),
          OutlinedButton(
            onPressed: () async {
              final res = await ref.read(dioProvider).post('/api/v1/matches/private/${code.text}/join');
              if (context.mounted) context.go('/match/${res.data['matchId']}');
            },
            child: const Text('Unirme'),
          ),
        ]),
      ),
    );
  }
}
