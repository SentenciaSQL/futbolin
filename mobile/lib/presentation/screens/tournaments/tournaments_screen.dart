import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class TournamentsScreen extends ConsumerWidget {
  const TournamentsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Torneos 16')),
      body: FutureBuilder(
        future: ref.watch(dioProvider).get('/api/v1/tournaments'),
        builder: (c, snap) {
          if (!snap.hasData) return const Center(child: CircularProgressIndicator());
          final list = (snap.data!.data as List?) ?? [];
          if (list.isEmpty) return const Center(child: Text('No hay copas abiertas'));
          return ListView.builder(
            itemCount: list.length,
            itemBuilder: (_, i) {
              final row = list[i] as Map;
              return ListTile(
                leading: const Icon(Icons.emoji_events),
                title: Text('${row['name'] ?? ''}'),
                subtitle: Text('${row['status'] ?? ''} · ${row['size'] ?? 16} jugadores'),
                onTap: () => context.push('/tournaments/${row['id']}'),
              );
            },
          );
        },
      ),
    );
  }
}

class TournamentDetailScreen extends ConsumerStatefulWidget {
  const TournamentDetailScreen({super.key, required this.id});
  final String id;
  @override
  ConsumerState<TournamentDetailScreen> createState() => _TournamentDetailScreenState();
}

class _TournamentDetailScreenState extends ConsumerState<TournamentDetailScreen> {
  Map<String, dynamic>? data;
  String? error;

  @override
  void initState() {
    super.initState();
    Future.microtask(_load);
  }

  Future<void> _load() async {
    final res = await ref.read(dioProvider).get('/api/v1/tournaments/${widget.id}');
    setState(() => data = Map<String, dynamic>.from(res.data as Map));
  }

  @override
  Widget build(BuildContext context) {
    final t = data?['tournament'] as Map? ?? {};
    final matches = (data?['matches'] as List?) ?? [];
    return Scaffold(
      appBar: AppBar(title: Text('${t['name'] ?? 'Torneo'}')),
      body: data == null
          ? const Center(child: CircularProgressIndicator())
          : ListView(padding: const EdgeInsets.all(16), children: [
              Text('${t['status']} · ${t['theme']}', style: const TextStyle(fontWeight: FontWeight.bold)),
              if (error != null) Text(error!, style: const TextStyle(color: Colors.redAccent)),
              const SizedBox(height: 8),
              FilledButton(
                onPressed: () async {
                  try {
                    await ref.read(dioProvider).post('/api/v1/tournaments/${widget.id}/join');
                    await _load();
                  } catch (e) {
                    setState(() => error = '$e');
                  }
                },
                child: const Text('Inscribirme'),
              ),
              const SizedBox(height: 16),
              const Text('Cuadro', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              ...matches.map((raw) {
                final m = Map<String, dynamic>.from(raw as Map);
                return ListTile(
                  title: Text('${m['roundName']} · slot ${m['slot']}'),
                  subtitle: Text('${m['status']}'),
                  trailing: m['status'] == 'READY'
                      ? TextButton(
                          onPressed: () async {
                            final res = await ref.read(dioProvider).post('/api/v1/tournaments/matches/${m['id']}/play');
                            if (context.mounted) context.push('/match/${res.data['matchId']}');
                          },
                          child: const Text('Jugar'),
                        )
                      : null,
                );
              }),
            ]),
    );
  }
}
