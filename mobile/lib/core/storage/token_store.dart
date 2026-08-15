import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStore {
  TokenStore(this._storage);
  final FlutterSecureStorage _storage;

  Future<void> save({required String access, required String refresh}) async {
    await _storage.write(key: 'access', value: access);
    await _storage.write(key: 'refresh', value: refresh);
  }

  Future<String?> accessToken() => _storage.read(key: 'access');
  Future<String?> refreshToken() => _storage.read(key: 'refresh');
  Future<void> clear() async => _storage.deleteAll();
}
