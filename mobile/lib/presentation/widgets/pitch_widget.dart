import 'package:flutter/material.dart';
import 'package:futbolin/core/theme/app_theme.dart';

class PitchWidget extends StatelessWidget {
  const PitchWidget({super.key, required this.position, required this.possessionUserId});
  final int position;
  final String possessionUserId;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 120,
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        gradient: const LinearGradient(colors: [Color(0xFF0E5A38), Color(0xFF178A4F), Color(0xFF0E5A38)]),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white24),
      ),
      child: Stack(children: [
        Row(children: List.generate(5, (i) => Expanded(child: Container(decoration: BoxDecoration(border: Border.all(color: Colors.white10)))))),
        Align(
          alignment: Alignment((position.clamp(-2, 2)) / 2, 0),
          child: const Text('⚽', style: TextStyle(fontSize: 28)),
        ),
        const Align(alignment: Alignment.centerLeft, child: Padding(padding: EdgeInsets.all(6), child: Text('A'))),
        const Align(alignment: Alignment.centerRight, child: Padding(padding: EdgeInsets.all(6), child: Text('B'))),
      ]),
    );
  }
}
