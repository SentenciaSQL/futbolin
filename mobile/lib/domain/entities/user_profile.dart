class UserProfile {
  UserProfile({
    required this.id,
    required this.username,
    required this.displayName,
    required this.level,
    required this.xp,
    required this.coins,
    required this.division,
    required this.rankingPoints,
    required this.dailyStreak,
    required this.avatarKey,
    this.favoriteTeam,
    this.country,
  });

  final String id;
  final String username;
  final String displayName;
  final int level;
  final int xp;
  final int coins;
  final String division;
  final int rankingPoints;
  final int dailyStreak;
  final String avatarKey;
  final String? favoriteTeam;
  final String? country;

  factory UserProfile.fromJson(Map<String, dynamic> json) => UserProfile(
        id: json['id'].toString(),
        username: json['username'] ?? '',
        displayName: json['displayName'] ?? '',
        level: json['level'] ?? 1,
        xp: json['xp'] ?? 0,
        coins: json['coins'] ?? 0,
        division: json['division'] ?? 'AMATEUR',
        rankingPoints: json['rankingPoints'] ?? 1000,
        dailyStreak: json['dailyStreak'] ?? 0,
        avatarKey: json['avatarKey'] ?? 'default',
        favoriteTeam: json['favoriteTeam'],
        country: json['country'],
      );
}
