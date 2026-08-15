import 'package:dio/dio.dart';
import 'package:futbolin/core/storage/token_store.dart';
import 'package:futbolin/domain/entities/user_profile.dart';

class AuthRepository {
  AuthRepository(this._dio, this._tokens);
  final Dio _dio;
  final TokenStore _tokens;

  Future<void> login(String login, String password) async {
    final res = await _dio.post('/api/v1/auth/login', data: {'login': login, 'password': password});
    await _tokens.save(access: res.data['accessToken'], refresh: res.data['refreshToken']);
  }

  Future<void> register({
    required String email,
    required String username,
    required String password,
  }) async {
    final res = await _dio.post('/api/v1/auth/register', data: {
      'email': email,
      'username': username,
      'password': password,
      'displayName': username,
    });
    await _tokens.save(access: res.data['accessToken'], refresh: res.data['refreshToken']);
  }

  Future<UserProfile> me() async {
    final res = await _dio.get('/api/v1/users/me');
    return UserProfile.fromJson(Map<String, dynamic>.from(res.data as Map));
  }

  Future<void> logout() => _tokens.clear();
}
