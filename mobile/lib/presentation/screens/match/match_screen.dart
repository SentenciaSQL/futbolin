import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/l10n/app_strings.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:futbolin/core/theme/app_theme.dart';
import 'package:futbolin/presentation/providers/locale_provider.dart';
import 'package:futbolin/presentation/widgets/pitch_widget.dart';
import 'package:go_router/go_router.dart';
import 'package:web_socket_channel/web_socket_channel.dart';

class MatchScreen extends ConsumerStatefulWidget {
  const MatchScreen({super.key, required this.matchId});
  final String matchId;
  @override
  ConsumerState<MatchScreen> createState() => _MatchScreenState();
}

class _MatchScreenState extends ConsumerState<MatchScreen> {
  WebSocketChannel? _channel;
  Map<String, dynamic> state = {};
  Map<String, dynamic>? question;
  String? celebration;
  bool finished = false;

  @override
  void initState() {
    super.initState();
    Future.microtask(_connect);
  }

  Future<void> _connect() async {
    final token = await ref.read(tokenStoreProvider).accessToken();
    final uri = Uri.parse(const String.fromEnvironment('WS_URL', defaultValue: 'ws://localhost:8080/ws/match?token='FAKESECRET_q2r3s4t5u6v7w8x9y0z1''));
    _channel = WebSocketChannel.connect(uri);
    _channel!.stream.listen((raw) {
      final msg = jsonDecode(raw as String) as Map<String, dynamic>;
      setState(() {
        final type = msg['type'];
        if (type == 'QUESTION') {
          question = Map<String, dynamic>.from(msg['question'] as Map);
          state = {...state, ...msg};
          celebration = null;
        } else if (type == 'ANSWER_RESULT' || type == 'GOAL' || type == 'SCORE_UPDATED') {
          state = {...state, ...msg};
          if (msg['goal'] == true) {
            celebration = 'GOAL';
            HapticFeedback.heavyImpact();
          }
        } else if (type == 'MATCH_FINISHED') {
          finished = true;
          state = {...state, ...msg};
        } else if (type == 'PLAYER_DISCONNECTED') {
          celebration = 'RECONNECT';
        } else {
          state = {...state, ...msg};
        }
      });
    });
  }

  void _answer(String key) {
    _channel?.sink.add(jsonEncode({
      'type': 'ANSWER',
      'matchId': widget.matchId,
      'roundId': state['roundId'],
      'optionKey': key,
    }));
  }

  @override
  void dispose() {
    _channel?.sink.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(ref.watch(localeProvider));
    final scoreA = state['scoreA'] ?? 0;
    final scoreB = state['scoreB'] ?? 0;
    final pos = state['ballPosition'] ?? 0;
    return Scaffold(
      body: SafeArea(
        child: Column(children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
              Text('$scoreA', style: const TextStyle(fontSize: 32, fontWeight: FontWeight.w900)),
              Column(children: [
                Text('${state['phase'] ?? 'KICKOFF'}', style: const TextStyle(color: AppTheme.gold, fontWeight: FontWeight.bold)),
                Text('${state['seconds'] ?? 10}s'),
              ]),
              Text('$scoreB', style: const TextStyle(fontSize: 32, fontWeight: FontWeight.w900)),
            ]),
          ),
          PitchWidget(position: pos is int ? pos : int.tryParse('$pos') ?? 0, possessionUserId: '${state['possessionUserId'] ?? ''}'),
          if (celebration == 'GOAL')
            Padding(
              padding: const EdgeInsets.all(8),
              child: Text(s.goal, style: const TextStyle(fontSize: 28, color: AppTheme.gold, fontWeight: FontWeight.w900)),
            ),
          if (celebration == 'RECONNECT') Text(s.reconnecting),
          Expanded(child: _questionCard()),
          if (finished)
            Padding(
              padding: const EdgeInsets.all(16),
              child: Row(children: [
                Expanded(child: FilledButton(onPressed: () => _channel?.sink.add(jsonEncode({'type': 'REMATCH', 'matchId': widget.matchId})), child: Text(s.rematch))),
                const SizedBox(width: 8),
                Expanded(child: OutlinedButton(onPressed: () => context.go('/queue'), child: Text(s.findAnother))),
              ]),
            )
          else
            Padding(
              padding: const EdgeInsets.all(8),
              child: Wrap(spacing: 8, children: [
                _emoji('👏'), _emoji('😂'), _emoji('😱'), _emoji('🔥'), _emoji('⚽'),
              ]),
            ),
        ]),
      ),
    );
  }

  Widget _questionCard() {
    if (question == null) return const Center(child: Text('Esperando saque inicial...'));
    final options = (question!['options'] as List?) ?? [];
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        Text('${question!['prompt']}', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w600)),
        const SizedBox(height: 12),
        ...options.map((o) {
          final map = Map<String, dynamic>.from(o as Map);
          return Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: FilledButton.tonal(
              onPressed: () => _answer('${map['key']}'),
              child: Align(alignment: Alignment.centerLeft, child: Text('${map['key']}. ${map['text']}')),
            ),
          );
        }),
        if (state['explanationEs'] != null)
          Padding(
            padding: const EdgeInsets.only(top: 12),
            child: Text('${state['explanationEs']}', style: const TextStyle(color: Colors.white70)),
          ),
      ],
    );
  }

  Widget _emoji(String code) {
    return ActionChip(label: Text(code), onPressed: () {
      _channel?.sink.add(jsonEncode({'type': 'EMOJI', 'matchId': widget.matchId, 'code': code}));
    });
  }
}
