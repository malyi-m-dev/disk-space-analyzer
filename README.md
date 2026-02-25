# Disk Space Analyzer (Windows)

Desktop-приложение на `Kotlin + Compose Multiplatform` для анализа занятого места на дисках/в папках Windows.

Приложение умеет:

- сканировать выбранную папку
- сканировать выбранные диски
- сканировать все диски
- показывать список крупных файлов
- показывать drill-down по папкам + круговую диаграмму
- удалять файлы/папки:
  - в корзину
  - навсегда
- делать `Рескан` с экрана результатов
- перемещаться по папкам стрелками (`←`, `→`, `↑`) на экране результатов

## Технологии

- `Kotlin`
- `Compose Multiplatform (Desktop)`
- `Compose Navigation` (стандартная compose navigation, без Voyager)
- `Koin`
- `JNA` (для удаления в корзину)

## Архитектура

Проект сделан в стиле референса `urentbike-technic`:

- `feature/api + feature/impl`
- `presentation/.../mvi`
  - `State`
  - `Event`
  - `SideEffect`
  - `ScreenModel`
- `domain/interactor`
- `di/Feature*Module`

### Модули

- `app_desktop` — точка входа desktop-приложения, Koin bootstrap
- `feature_disk_analyzer:api` — публичный feature contract
- `feature_disk_analyzer:impl` — реализация экранов, MVI, interactor'ы
- `data_filesystem:api` / `data_filesystem:impl` — сканирование файловой системы
- `core_platform_windows` — Windows-specific сервисы (Explorer, folder picker, drives, delete)
- `core_base_feature` — базовый `BaseScreenModelV2`
- `core_utils` — утилиты (например форматирование размера)

## Что уже реализовано

### Экран настройки

- ввод пути вручную
- выбор папки через системный диалог (`JFileChooser`)
- список доступных дисков с чекбоксами
- запуск сканирования:
  - выбранной папки
  - выбранных дисков
  - всех дисков

### Экран сканирования

- прогресс сканирования (счетчики файлов/папок/байт)
- текущий путь
- отмена сканирования

### Экран результатов

- список крупных файлов
- drill-down по структуре папок
- круговая диаграмма текущей папки
- навигация по истории (`←`, `→`) и к родителю (`↑`)
- кнопка `Рескан`
- кнопка `На старт`
- удаление:
  - в корзину
  - навсегда
- локальное обновление snapshot после удаления (без полного перескана)

## Производительность (текущие улучшения)

Сейчас уже добавлено:

- ограниченный параллелизм при сканировании директорий
- throttling обновления прогресса (меньше нагрузки на UI)
- локальное обновление данных после удаления вместо полного перескана

## Требования

- Windows 10/11
- JDK 21

Проверено на запуск через Gradle task `:app_desktop:run`.

## Запуск

```powershell
cd C:\Users\maxim\disk-space-analyzer
.\gradlew.bat :app_desktop:run
```

## Сборка

```powershell
cd C:\Users\maxim\disk-space-analyzer
.\gradlew.bat :app_desktop:build -x test
```

## Важные замечания

- Удаление выполняется сразу по клику (confirm-диалог пока не добавлен).
- Для некоторых путей Windows возможны ошибки доступа (`Access Denied`) — это нормальный сценарий.
- Символические ссылки / reparse points в обход не включаются (для безопасности и во избежание циклов).

## Ближайшие улучшения (roadmap)

- confirm-диалог перед удалением
- multi-select удаление
- фильтры/исключения для сканирования (`node_modules`, `.git`, `build`, temp-папки)
- более умные рекомендации по очистке
- горячие клавиши навигации (`Alt+Left`, `Alt+Right`, `Alt+Up`)

