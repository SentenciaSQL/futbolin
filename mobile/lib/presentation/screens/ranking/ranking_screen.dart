import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:go_router/go_router.dart';

class RankingScreen extends ConsumerWidget {
  const RankingScreen({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Ranking'),
          bottom: const TabBar(tabs: [
            Tab(text: 'Global'),
            Tab(text: 'Semanal'),
            Tab(text: 'Amigos'),
          ]),
        ),
        body: TabBarView(children: [
          _RankingList(future: ref.watch(dioProvider).get('/api/v1/rankings'), page: true),
          _RankingList(future: ref.watch(dioProvider).get('/api/v1/rankings/weekly'), page: false, pointsKey: 'wins'),
          _RankingList(future: ref.watch(dioProvider).get('/api/v1/rankings/friends'), page: false),
        ]),
      ),
    );
  }
}

class _RankingList extends StatelessWidget {
  const _RankingList({required this.future, required this.page, this.pointsKey = 'rankingPoints'});
  final Future future;
  final bool page;
  final String pointsKey;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: future,
      builder: (c, snap) {
        if (!snap.hasData) return const Center(child: CircularProgressIndicator());
        final data = snap.data!.data;
        final content = page ? ((data['content'] as List?) ?? []) : ((data as List?) ?? []);
        return ListView.builder(
          itemCount: content.length,
          itemBuilder: (_, i) {
            final row = content[i] as Map;
            final userId = '${row['userId'] ?? row['id'] ?? ''}';
            return ListTile(
              leading: Text('#${row['rank'] ?? i + 1}'),
              title: Text('${row['displayName'] ?? row['username'] ?? ''}'),
              subtitle: Text('${row['division'] ?? ''}'),
              trailing: Text('${row[pointsKey] ?? row['rankingPoints'] ?? row['points'] ?? ''}'),
              onTap: userId.isEmpty ? null : () => context.push('/users/$userId'),
            );
          },
        );
      },
    );
  }
}
