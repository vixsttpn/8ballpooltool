# 8Ball Pro v4.0 — Инструкция по загрузке на GitHub

## СТРУКТУРА РЕПОЗИТОРИЯ

Твой репозиторий должен выглядеть ТОЧНО ТАК:

```
(корень репозитория)
├── codemagic.yaml          ← ЭТО ОБЯЗАТЕЛЬНО В КОРНЕ!
└── 8BallPro/
    ├── build.gradle
    ├── settings.gradle
    ├── gradle.properties   ← КЛЮЧЕВОЙ ФАЙЛ (без него билд не пройдёт)
    ├── gradle/
    │   └── wrapper/
    │       └── gradle-wrapper.properties
    └── app/
        ├── build.gradle
        ├── proguard-rules.pro
        └── src/main/
            ├── AndroidManifest.xml
            ├── java/com/eightball/pro/
            │   ├── model/Models.kt
            │   ├── engine/PhysicsEngine.kt
            │   ├── engine/Renderer.kt
            │   ├── overlay/OverlayView.kt
            │   ├── overlay/OverlayService.kt
            │   ├── ui/MainActivity.kt
            │   └── ui/TutorialActivity.kt
            └── res/
                ├── values/themes.xml
                ├── values/strings.xml
                ├── values/colors.xml
                ├── drawable/ic_launcher_background.xml
                ├── drawable/ic_launcher_foreground.xml
                ├── mipmap-anydpi-v26/ic_launcher.xml
                ├── mipmap-anydpi-v26/ic_launcher_round.xml
                └── anim/fade_in.xml
```

## КАК ЗАГРУЗИТЬ (способ 1 — через браузер)

1. Зайди на https://github.com/vixsttpn/8BallProo
2. Удали ВСЕ старые файлы (Settings → Delete this repository → создай новый)
   ИЛИ обнови каждый файл через карандаш ✏️
3. Загрузи файлы в точности по структуре выше

## КАК ЗАГРУЗИТЬ (способ 2 — через Git, рекомендуется)

```bash
git clone https://github.com/vixsttpn/8BallProo
cd 8BallProo
# Удали старые файлы, скопируй новые из этого ZIP
git add -A
git commit -m "v4.0 - full rewrite, fixes all compile errors"
git push
```

## ПОСЛЕ ЗАГРУЗКИ

1. Зайди на https://codemagic.io
2. Нажми "Start new build"
3. Выбери ветку main
4. Дождись зелёного билда (~2 минуты)
5. Скачай APK!

## ЧТО ИСПРАВЛЕНО В v4.0

✅ Val cannot be reassigned — все var/val исправлены
✅ android.useAndroidX — gradle.properties добавлен
✅ ControlPanel убран (встроен в OverlayService)
✅ Все lateinit заменены на nullable безопасные
✅ dataBinding=false — убрана зависимость
✅ Чистый Kotlin без лишних зависимостей
