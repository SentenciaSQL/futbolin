import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:futbolin/core/l10n/app_strings.dart';
import 'package:futbolin/core/router/app_router.dart';
import 'package:futbolin/core/theme/app_theme.dart';
import 'package:futbolin/presentation/providers/locale_provider.dart';

class FutbolinApp extends ConsumerWidget {
  const FutbolinApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeProvider);
    final router = ref.watch(appRouterProvider);
    return MaterialApp.router(
      title: AppStrings.of(locale).appName,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.dark(),
      locale: locale,
      routerConfig: router,
    );
  }
}
