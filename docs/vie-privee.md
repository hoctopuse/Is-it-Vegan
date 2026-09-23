# Vie privée et fonctionnement hors ligne

Le code métier est conçu pour fonctionner sans service applicatif distant, mais le manifeste final autorise le réseau à cause de dépendances ML Kit. Il faut conserver cette nuance dans toute communication utilisateur.

## Traitement local confirmé par le dépôt

- La photo est choisie avec le Photo Picker Android puis lue par `ContentResolver`.
- L’orientation, le redimensionnement et le recadrage sont effectués avec `ExifInterface` et `Bitmap` sur l’appareil.
- L’OCR utilise `com.google.mlkit:text-recognition:16.0.1`. L’arbre Gradle contient `text-recognition-bundled-common`, ce qui confirme l’intégration du modèle latin embarqué.
- La segmentation, le nettoyage, le parsing, le matching et le verdict sont du code Kotlin local.
- `ingredients.json` et `origin_qualifier_rules.json` sont des assets de l’APK.
- Le dépôt ne contient aucun client HTTP, endpoint métier, clé d’API, LLM ou appel de traduction.

L’application n’écrit pas la photo recadrée dans la galerie. `OcrSession` exclut volontairement l’URI ; les bitmaps et textes OCR restent dans l’état mémoire de l’écran. Une nouvelle photo ou une remise à zéro remplace cet état.

## Données persistées

Le choix de langue de l’interface est enregistré dans `SharedPreferences` sous `ui_preferences/interface_language`. Les textes OCR et résultats ne sont pas écrits explicitement dans un fichier ou une base locale par le code examiné.

L’application déclare toutefois `allowBackup="true"`. Les fichiers `backup_rules.xml` et `data_extraction_rules.xml` sont les modèles par défaut et n’excluent pas explicitement les préférences. Il n’est donc pas possible d’affirmer depuis le dépôt que toute donnée persistante est exclue des mécanismes de sauvegarde Android.

## Export volontaire

Les boutons d’export construisent un texte et lancent `Intent.ACTION_SEND`. À partir de ce moment, la destination choisie par l’utilisateur peut être une application locale ou un service en ligne. L’export OCR ne contient pas le bitmap, mais contient le texte brut, le texte édité et les diagnostics.

## Dépendances et réseau

Le manifeste source de l’application ne déclare pas directement `INTERNET`. Le manifeste **fusionné** de la variante debug contient néanmoins :

- `android.permission.INTERNET` ;
- `android.permission.ACCESS_NETWORK_STATE` ;
- les services de Google Data Transport/CCT apportés transitivement par ML Kit.

La résolution Gradle montre notamment `com.google.android.datatransport:transport-backend-cct`. Le code du dépôt ne programme aucun envoi et ne configure pas d’outil analytics, mais la présence de ces composants tiers signifie que l’absence absolue de télémétrie ou de communication réseau ne peut pas être garantie par la seule inspection du code applicatif.

Le fichier manifeste contient aussi la métadonnée `com.google.android.gms.vision.DEPENDENCIES=ocr`, bien que l’artefact choisi soit la variante embarquée. Toute évolution de la dépendance ML Kit ou de cette métadonnée doit être revue avec le manifeste fusionné, pas seulement le manifeste source.

## Formulation maintenable

La formulation techniquement défendable est : le traitement fonctionnel de l’étiquette ne dépend d’aucune API métier distante et peut s’exécuter avec les modèles et données embarqués. Il ne faut pas promettre « aucune connexion réseau » tant que les permissions et composants de transport transitifs restent dans l’APK.

