import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/network/dio_client.dart';
import 'package:futbolin/data/repositories/auth_repository.dart';
import 'package:futbolin/domain/entities/user_profile.dart';

final authRepositoryProvider = Provider((ref) => AuthRepository(ref.watch(dioProvider), ref.watch(tokenStoreProvider)));

final sessionProvider = FutureProvider<UserProfile?>((ref) async {
  final token = await ref.watch(tokenStoreProvider).accessToken();
  if (token == null) return null;
  try {
    return await ref.watch(authRepositoryProvider).me();
  } catch (_) {
    return null;
  }
});
