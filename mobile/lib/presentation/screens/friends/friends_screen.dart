import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class FriendsScreen extends ConsumerStatefulWidget {
  const FriendsScreen({super.key});
  @override
  ConsumerState<FriendsScreen> createState() => _FriendsScreenState();
}

class _FriendsScreenState extends ConsumerState<FriendsScreen> {
  final username = TextEditingController();
  Object? friends;
  Object? pending;
  String? error;

  @override
  void initState() {
    super.initState();
    Future.microtask(_reload);
  }

  Future<void> _reload() async {
    final dio = ref.read(dioProvider);
    final f = await dio.get('/api/v1/friends');
    final p = await dio.get('/api/v1/friends/pending');
    setState(() {
      friends = f.data;
      pending = p.data;
    });
  }

  @override
  Widget build(BuildContext context) {
    final list = (friends as List?) ?? [];
    final waits = (pending as List?) ?? [];
    return Scaffold(
      appBar: AppBar(title: const Text('Amigos')),
      body: RefreshIndicator(
        onRefresh: _reload,
        child: ListView(padding: const EdgeInsets.all(16), children: [
          TextField(
            controller: username,
            decoration: const InputDecoration(hintText: 'Usuario a agregar'),
          ),
          const SizedBox(height: 8),
          FilledButton(
            onPressed: () async {
              try {
                await ref.read(dioProvider).post('/api/v1/friends', data: {'username': username.text});
                username.clear();
                await _reload();
              } catch (e) {
                setState(() => error = '$e');
              }
            },
            child: const Text('Enviar solicitud'),
          ),
          if (error != null) Text(error!, style: const TextStyle(color: Colors.redAccent)),
          const SizedBox(height: 16),
          const Text('Pendientes', style: TextStyle(fontWeight: FontWeight.bold)),
          ...waits.map((raw) {
            final row = Map<String, dynamic>.from(raw as Map);
            return ListTile(
              title: Text('${row['requesterId']} → ${row['addresseeId']}'),
              trailing: TextButton(
                onPressed: () async {
                  await ref.read(dioProvider).post('/api/v1/friends/${row['id']}/accept');
                  await _reload();
                },
                child: const Text('Aceptar'),
              ),
            );
          }),
          const Divider(),
          const Text('Ranking de amigos', style: TextStyle(fontWeight: FontWeight.bold)),
          ...list.asMap().entries.map((e) {
            final row = Map<String, dynamic>.from(e.value as Map);
            final online = row['online'] == true;
            return ListTile(
              leading: Text('#${e.key + 1}'),
              title: Text('${row['displayName'] ?? ''}'),
              subtitle: Text('${row['division'] ?? ''} · ${online ? 'en línea' : 'offline'}'),
              trailing: Text('${row['rankingPoints'] ?? ''}'),
              onTap: () => context.push('/users/${row['userId']}'),
            );
          }),
        ]),
      ),
    );
  }
}
