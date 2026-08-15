import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:futbolin/presentation/providers/session_provider.dart';
import 'package:futbolin/presentation/screens/auth/auth_screen.dart';
import 'package:futbolin/presentation/screens/daily/daily_screen.dart';
import 'package:futbolin/presentation/screens/friends/friends_screen.dart';
import 'package:futbolin/presentation/screens/history/history_screen.dart';
import 'package:futbolin/presentation/screens/home/home_screen.dart';
import 'package:futbolin/presentation/screens/match/match_screen.dart';
import 'package:futbolin/presentation/screens/match/queue_screen.dart';
import 'package:futbolin/presentation/screens/missions/missions_screen.dart';
import 'package:futbolin/presentation/screens/private/private_match_screen.dart';
import 'package:futbolin/presentation/screens/profile/profile_screen.dart';
import 'package:futbolin/presentation/screens/profile/public_profile_screen.dart';
import 'package:futbolin/presentation/screens/ranking/ranking_screen.dart';
import 'package:futbolin/presentation/screens/rivalries/rivalries_screen.dart';
import 'package:futbolin/presentation/screens/stats/stats_screen.dart';
import 'package:futbolin/presentation/screens/store/store_screen.dart';
import 'package:futbolin/presentation/screens/survival/survival_screen.dart';
import 'package:futbolin/presentation/screens/tournaments/tournaments_screen.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final session = ref.watch(sessionProvider);
  return GoRouter(
    initialLocation: '/home',
    redirect: (context, state) {
      final loggingIn = state.matchedLocation == '/auth';
      final authed = session.asData?.value != null;
      if (!authed && !loggingIn && !session.isLoading) return '/auth';
      if (authed && loggingIn) return '/home';
      return null;
    },
    routes: [
      GoRoute(path: '/auth', builder: (c, s) => const AuthScreen()),
      GoRoute(path: '/home', builder: (c, s) => const HomeScreen()),
      GoRoute(path: '/queue', builder: (c, s) => const QueueScreen()),
      GoRoute(path: '/match/:id', builder: (c, s) => MatchScreen(matchId: s.pathParameters['id']!)),
      GoRoute(path: '/private', builder: (c, s) => const PrivateMatchScreen()),
      GoRoute(path: '/ranking', builder: (c, s) => const RankingScreen()),
      GoRoute(path: '/missions', builder: (c, s) => const MissionsScreen()),
      GoRoute(path: '/profile', builder: (c, s) => const ProfileScreen()),
      GoRoute(path: '/store', builder: (c, s) => const StoreScreen()),
      GoRoute(path: '/stats', builder: (c, s) => const StatsScreen()),
      GoRoute(path: '/daily', builder: (c, s) => const DailyScreen()),
      GoRoute(path: '/survival', builder: (c, s) => const SurvivalScreen()),
      GoRoute(path: '/history', builder: (c, s) => const HistoryScreen()),
      GoRoute(path: '/friends', builder: (c, s) => const FriendsScreen()),
      GoRoute(path: '/tournaments', builder: (c, s) => const TournamentsScreen()),
      GoRoute(path: '/tournaments/:id', builder: (c, s) => TournamentDetailScreen(id: s.pathParameters['id']!)),
      GoRoute(path: '/users/:id', builder: (c, s) => PublicProfileScreen(userId: s.pathParameters['id']!)),
      GoRoute(path: '/rivalries', builder: (c, s) => const RivalriesScreen()),
    ],
  );
});
