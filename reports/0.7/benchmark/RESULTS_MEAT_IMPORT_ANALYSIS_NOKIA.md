# Résultats — impact des imports animaux sur l’analyse

## Périmètre et état

Mesures physiques réellement exécutées le 8 octobre 2026 sur le Nokia G42 5G, Android 15, serial `AQ5003H044Q32600084`, avec le même test instrumenté, les mêmes textes et le même runner. La machine hôte est le Lenovo; les temps Gradle n’ont pas été utilisés comme mesures Android. Aucun APK ne contient `5087.jpg` et aucune mesure ne lance ML Kit.

Commits comparés : pré-import `6be60933df464dccb857b8d3287825011567344b` (parent de `fc74f08aa87e22c0fe38cf896a1a323c64cbf7ea`, premier import animal ciblé dans l’historique retenu) et post-import `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. Les deux hashes existent; deux worktrees détachés ont été utilisés et conservés. `app/src/main` et `mutation-core/src/main` ne présentent aucun changement entre ces commits. La variation des données couvre toutefois les enrichissements animaux/alimentaires de l’intervalle, pas uniquement une variable « viande » isolée.

Données : pré 479 concepts, 1 996 mappings, 1 668 groupes d’alias; assets UTF-8 `ingredients.json` 228 147 octets, mappings 911 829, règles 14 906. Post 488 concepts, 2 093 mappings, 1 707 groupes d’alias; respectivement 233 391, 997 318 et 14 906 octets.

ADB a confirmé avant les opérations appareil `device`, modèle `Nokia G42 5G`, Android 15. Relevés conservés sous `app/build/benchmark-artifacts/` dans chaque worktree : batterie/état thermique avant et après. Lors du relevé récent : batterie 100 %, alimentation secteur oui, USB powered non, état thermique 0, batterie 28,3 °C. Aucun incident thermique connu. Les traces ont été exécutées séquentiellement; elles ne constituent pas une mesure du Pixel.

## Résultats moteur

Les durées ci-dessous sont des secondes, sauf extraction de section (ms). `cold` désigne le premier appel dans le processus instrumenté après chargement des assets, et non un démarrage froid Android. Chaque analyse complète a eu 5 échauffements puis 10 mesures. Médiane basse = 5e valeur triée. Le test vérifie le résultat hors chronométrage. Le parsing isolé est mesuré avec la même série. Les valeurs brutes d’analyse et de parsing sont données ci-dessous dans l’ordre d’exécution.

### Pré-import — `6be60933df464dccb857b8d3287825011567344b`

- Lecture asset cold: 0,057388 s. Chargement connaissance cold: 1,403000 s; warm min / médiane basse / médiane / max: 1,002542 / 1,005667 / 1,006133 / 1,016660 s.
- Construction matcher cold: 1,198800 s; warm min / médiane basse / médiane / max: 1,090649 / 1,097404 / 1,098724 / 1,103090 s. Bruts ns: `1097404010,1102338749,1095870052,1102134687,1100624843,1103089739,1100044479,1094923542,1093021875,1090648802`.
- Texte corrigé: cold 10,917288 s; warm min / médiane basse / médiane / max: 10,431627 / 10,449751 / 10,451358 / 10,483457 s. Bruts ns: `10483457496,10461838902,10444158173,10431626662,10447240152,10452964735,10463191193,10449751142,10448452236,10453595256`.
- `Glucose-fructose syna`: cold 10,462047; warm 10,434713 / 10,463620 / 10,464646 / 10,484374. Bruts ns: `10479106194,10466749110,10463619840,10484374163,10476071975,10454415882,10451256714,10434712652,10465672549,10456592704`.
- `WHEAT lour`: cold 10,451910; warm 10,427614 / 10,447316 / 10,449964 / 10,463938. Bruts ns: `10452611559,10463716871,10463938017,10436549840,10427614215,10445199162,10447315621,10437570465,10453473121,10458315986`.
- `CTitric aid`: cold 10,497573; warm 10,473182 / 10,477422 / 10,484250 / 10,508095. Bruts ns: `10496021663,10473182079,10473987444,10477422444,10494208068,10504852080,10508095413,10474435100,10475995204,10491076923`.
- `Favouring`: cold 10,442694; warm 10,407003 / 10,425644 / 10,426316 / 10,624487. Bruts ns: `10426989111,10624486663,10422477965,10446239944,10434970881,10429437912,10407683798,10410640674,10407003485,10425643642`.
- Parsing seul cold 5,605640 s; warm 5,582553 / 5,586877 / 5,587799 / 5,600115. Bruts ns: `5582777133,5583347862,5586876821,5593069633,5588720571,5600114738,5599412758,5598193488,5582553123,5583976508`.
- Extraction de section cold 50,505 ms; warm 48,160 / 48,765 / 48,907 / 50,635 ms. Bruts ns: `48160052,48301563,50635365,48626875,49344792,49048021,48765417,49325312,49191927,48691927`.

### Post-import — `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`

- Lecture asset cold: 0,060981 s. Chargement connaissance cold: 1,450013 s; warm min / médiane basse / médiane / max: 1,037360 / 1,047729 / 1,049506 / 1,056215 s. Bruts ns: `1052934375,1056214479,1056214635,1052518802,1051283542,1047640833,1046718593,1047728750,1041468906,1037360312`.
- Construction matcher cold: 1,225178 s; warm min / médiane basse / médiane / max: 1,125636 / 1,132127 / 1,132199 / 1,136927 s. Bruts ns: `1136927447,1135648385,1128658906,1132127030,1134025052,1134490624,1129236458,1132271249,1131762083,1125635573`.
- Texte corrigé: cold 11,046453 s; warm 10,564791 / 10,576514 / 10,577653 / 10,590286 s. Bruts ns: `10585030100,10576513590,10581782392,10586454840,10575852912,10590286454,10578791819,10576473173,10564791089,10568745152`.
- `Glucose-fructose syna`: cold 10,602773; warm 10,567629 / 10,581052 / 10,581917 / 10,632533. Bruts ns: `10606376923,10589375881,10582781871,10575033642,10632532965,10567629059,10574345881,10590004944,10574116663,10581052080`.
- `WHEAT lour`: cold 10,571582; warm 10,541769 / 10,570858 / 10,571145 / 10,580399. Bruts ns: `10571432339,10575243486,10553766715,10572048225,10570857757,10580399163,10572493382,10552907235,10557044423,10541769059`.
- `CTitric aid`: cold 10,770497; warm 10,585791 / 10,603356 / 10,605295 / 10,684189. Bruts ns: `10595237132,10603356142,10607586610,10585790726,10612471037,10684188693,10603235569,10588289111,10633005725,10607233329`.
- `Favouring`: cold 10,547151; warm 10,517909 / 10,532559 / 10,535205 / 10,563389. Bruts ns: `10530884528,10532558537,10546769787,10537851767,10538370048,10531122131,10517909059,10525809111,10563389475,10546345465`.
- Parsing seul cold 5,602579 s; warm 5,595656 / 5,606645 / 5,606936 / 5,617447. Bruts ns: `5617446717,5608429269,5613495987,5606513384,5595655571,5606645102,5602059373,5601052341,5607225988,5607273331`.
- Extraction de section cold 48,363 ms; warm 48,514 / 49,107 / 49,149 / 49,830 ms. Bruts ns: `48589896,49829583,49190260,48796719,49281875,49107188,48703958,49382552,48514480,49564740`.

Les deux tests moteurs ciblés ont réussi (1 test chacun, 0 échec). XML : `.benchmark-worktrees/pre-meat-6be6093/app/build/outputs/androidTest-results/connected/debug/TEST-Nokia G42 5G - 15.xml` et équivalent sous `post-meat-3ec32d3`. Logs Gradle/logcat sous `app/build/benchmark-artifacts/` (`motor-final.log` pré, `motor-run.log` post, et fichiers `logcat-com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest-profilesRealLabelTextAndDeterministicOcrVariants.txt`). Résultats XML/logs restent dans les dossiers build ignorés des worktrees; worktrees conservés.

## Texte édité et clic réel

Le test ciblé `MainScreenInstrumentedTest#analysisClickUsesTheEditedOcrFieldValue` a réussi sur les deux commits (environ 7,8 s chacun). Il remplace le marqueur OCR par `Ingredients: water, sugar`, clique et confirme que le résultat affiché vient de la valeur éditée. Le code UI transmet `session.editableText`; un seul appel au service suit le garde `analyzing`. Le test valide le routage au niveau UI, pas par mock du service.

Deux tentatives Perfetto pré-import antérieures n’ont pas produit de clic analysable. La trace `real-label-click-pre-retry-20261008-190346.pftrace` montre la fermeture d’ActivityScenario sans analyse; la capture `real-label-click-pre-valid-20261008-191056.pftrace` (1 708 882 octets) contient 0 événement input dispatch, 0 motion et 0 key, et seulement quelques millisecondes d’activité du dispatcher. La capture s’est donc déroulée sans clic. Ces traces sont invalides pour attribuer la durée du clic. Une capture valide n’est pas encore disponible; le profilage post-import n’a pas été lancé.

Le test de préparation maintient l’écran prêt pendant 120 s. Le clic doit être manuel pendant une capture courte et ne doit être fait qu’une fois. L’utilisateur a été sollicité pour ce geste. Fichiers temporaires/captures et relevés thermiques/batterie sont dans les dossiers `app/build/benchmark-artifacts/` des worktrees, y compris les trois traces pré-import ci-dessus. La capture valide candidate est déjà analysée avec Trace Processor; elle ne montre aucun input.

## Interprétation et limites

Médiane warm du texte corrigé : 10,451358 s pré, 10,577653 s post, soit +126 ms (+1,21 %). Construction matcher warm : 1,098724 s puis 1,132199 s, +33 ms (+3,05 %). Parsing seul : 5,587799 s puis 5,606936 s, +19 ms (+0,34 %). Extraction de section : environ 49 ms sur les deux. Les variantes fautives ont des temps proches du texte corrigé; aucun chemin typo n’a montré ici un surcoût majeur.

La preuve physique indique un coût intrinsèque important dans le moteur, surtout dans le chemin mesuré comme parsing (environ 5,6 s) et la construction du matcher (environ 1,1 s), avec l’analyse complète autour de 10,5 s. La mesure du clic UI n’est pas valide : on ne peut donc pas encore chiffrer le délai entre le geste et l’affichage ni départager le temps moteur du rendu/ordonnancement. Les imports sont associés à une hausse faible de l’analyse warm et du matcher entre ces deux commits; l’intervalle contient des enrichissements de données plus larges et ne prouve pas que l’import viande seul explique la hausse ou l’attente perçue sur Pixel.

`DiagnosticReport.build`, PackageManager et rendu n’ont pas été séparément chronométrés. Pas de mesure mémoire fiable. Les deux échecs automatisés connus sont un chantier distinct; aucune suite complète n’a été lancée et leurs noms ne sont pas établis dans les artefacts examinés. La compilation hôte n’est pas utilisée comme mesure de performance. Aucun changement de production, moteur, matcher, import ou version, aucun commit/push.

## Commandes réellement utilisées et tests locaux

Les commandes Gradle instrumentées ont ciblé uniquement :

```powershell
$env:ANDROID_SERIAL = 'AQ5003H044Q32600084'
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.RealLabelMultilingualBenchmarkInstrumentedTest" --console=plain
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.example.isitvegan.MainScreenInstrumentedTest#analysisClickUsesTheEditedOcrFieldValue" --console=plain
```

Elles ont été lancées séparément dans les worktrees pré et post; les logs exacts sont `motor-final.log`/`motor-run.log` et `edited-text_click_test.log` sous leurs dossiers `app/build/benchmark-artifacts/` (les noms des fichiers de log du clic peuvent aussi être sous `app/build/`). ADB a été explicitement ciblé avec `adb -s AQ5003H044Q32600084`; identité vérifiée avant les opérations. Trace Processor utilisé localement : `trace_processor_shell.exe query <trace> <SQL>` sur la trace pré-import valide candidate.


## Reprise du profil UI post-import — 8 octobre 2026, bloquée avant capture

État Git inspecté avant action : branche principale `master`, HEAD `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`; worktrees conservés `pre-meat-6be6093` au `6be60933df464dccb857b8d3287825011567344b` et `post-meat-3ec32d3` au HEAD post-import. Les changements utilisateur/non suivis restent intacts. Aucun code n’a été modifié dans cette reprise.

ADB a listé uniquement le serial autorisé `AQ5003H044Q32600084`, état `device`; `getprop ro.product.model` confirme `Nokia G42 5G`, Android 15. La vérification en lecture seule a montré le launcher au premier plan. `adb -s AQ5003H044Q32600084 shell pm path com.example.isitvegan` n’a renvoyé aucun chemin et `pm list packages` ne montre aucun package Vegan installé. Aucun APK n’a été installé ou lancé et aucune capture Perfetto n’a été démarrée, conformément à la consigne de ne rien installer.

Le build local post-import existe au commit vérifié : `.benchmark-worktrees/post-meat-3ec32d3/app/build/outputs/apk/debug/app-debug.apk`, variante `debug`, applicationId `com.example.isitvegan`, versionCode 59, versionName `0.6.13.7`, SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`. Il s’agit d’un artefact local de build; la vérification ne prouve pas qu’il est installé sur le téléphone. Pour cette raison, il n’existe pas de nouvelle trace, d’événement d’entrée, ni de mesure clic-vers-résultat à analyser. Aucune interaction UI n’a été effectuée par l’agent et aucune capture échouée n’a été présentée comme mesure.

**Blocage précis / suite minimale :** le package n’est absent du Nokia. Une capture exploitable nécessite l’installation/lancement du build post-import, ce que la consigne actuelle exclut (« N’installe rien »). Une fois cette restriction levée ou l’application post-import installée par le propriétaire, la suite est de préparer le texte corrigé dans l’éditeur, démarrer Perfetto, attendre le clic manuel unique et arrêter la capture après l’affichage du résultat. Il faudra ensuite vérifier dans la trace l’événement d’entrée et l’activité app-owned du thread Default avant d’annoncer une durée. Les résultats moteur antérieurs ne remplacent pas cette mesure.

## Installation post-import vérifiée — 8 octobre 2026

L’APK locale préalablement construite a été validée puis installée conformément à l’autorisation reçue. Serial ciblé : `AQ5003H044Q32600084`; modèle `Nokia G42 5G`; Android 15; état ADB `device`. Worktree `post-meat-3ec32d3` HEAD `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`. APK `app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`, versionCode 59 / versionName `0.6.13.7`. Commande réellement exécutée : `adb -s AQ5003H044Q32600084 install <chemin absolu de cet app-debug.apk>`; retour `Success`. `pm path` confirme `/data/app/~~eo1eB9jUylWEx8_sWsbBxw==/com.example.isitvegan-fEKlaEKkzmi78AL-EBeAzQ==/base.apk`; `dumpsys package` confirme la version. L’application a été lancée et `MainActivity` est au premier plan.

La trace n’est pas encore démarrée : le prochain geste requis est la préparation manuelle par Sébastien de l’écran OCR avec le texte de benchmark, clavier fermé, sans lancer Analyse. La capture et le clic unique ne seront entrepris qu’après son signal que l’écran est prêt. Il n’y a donc pas encore de mesure clic-résultat dans cette reprise.

## Tentative de capture post-import — commande rejetée avant démarrage

Après confirmation de Sébastien que l’écran était prêt, état vérifié juste avant : Nokia G42 5G (`AQ5003H044Q32600084`), Android 15, app `0.6.13.7` dans `MainActivity`; batterie 100 %, AC powered true, USB powered false; Thermal Status 0 (relevé de batterie du service thermique 28,3 °C et 26,5 °C). Les relevés avant tentative sont conservés dans `app/build/benchmark-artifacts/ui-post-20261008-200354-battery-before.txt` et `ui-post-20261008-200354-thermal-before.txt`.

Commande tentée :

```powershell
adb -s AQ5003H044Q32600084 shell perfetto --no-clobber -o /data/misc/perfetto-traces/ui-post-20261008-200354.pftrace -t 120s --app com.example.isitvegan sched freq idle am wm gfx view input binder_driver dalvik
```

La CLI Perfetto Android a répondu `unrecognized option '--no-clobber'` et a affiché son aide. Le processus de trace n’a pas démarré; le fichier local attendu `app/build/benchmark-artifacts/ui-post-20261008-200354.pftrace` est absent. Aucun clic n’a été demandé à Sébastien, aucun événement n’a été enregistré et aucune mesure clic-résultat n’est revendiquée. La version embarquée de Perfetto ne prend pas cette option (contrairement à des documentations plus récentes). Conformément à la consigne, l’essai n’est pas relancé automatiquement. Il faut une nouvelle autorisation explicite pour lancer une tentative corrigée sans `--no-clobber`, avec un nom de trace unique vérifié à l’avance.

## Profil UI post-import — capture exploitable, 8 octobre 2026

**Conclusion courante :** trace exploitable pour estimer le délai entre le toucher et la première image d’interface après l’analyse. La capture n’a pas été arrêtée manuellement après confirmation du résultat : elle a atteint sa limite de 120 s faute de réponse utilisateur après la demande. L’analyse de trace montre néanmoins le geste et le rendu qui suit. Le délai rapporté ci-dessous est donc `ACTION_DOWN` → première frame résultat, dérivée des événements de trace; l’affichage sémantique n’a pas été confirmé séparément par une réponse ou une capture d’écran.

### Conditions et artefacts

- Nokia G42 5G, serial `AQ5003H044Q32600084`, Android 15, état ADB `device`; app `com.example.isitvegan` version `0.6.13.7`, `MainActivity` au premier plan; build `debug` du commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`.
- APK installée : `.benchmark-worktrees/post-meat-3ec32d3/app/build/outputs/apk/debug/app-debug.apk`, SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`.
- Sébastien a indiqué que l’éditeur était prêt avec le texte corrigé. Aucun événement UI n’a été injecté par l’agent pendant la capture.
- Avant : batterie 100 %, secteur oui, USB-powered non, température battery service 26,6 °C, état thermique 0 (températures thermique 28,3 °C et 26,6 °C). Après : batterie 100 %, secteur oui, USB-powered non, température battery service 26,5 °C, état thermique 0 (28,3 °C et 26,5 °C). Relevés `ui-post-20261008-200620-battery-before.txt`, `-battery-after.txt`, `-thermal-before.txt`, `-thermal-after.txt` dans `app/build/benchmark-artifacts/` du worktree post.
- Commande de capture réellement démarrée (Perfetto Android a renvoyé le PID background `18258`) :

```powershell
adb -s AQ5003H044Q32600084 shell perfetto --background -o /data/misc/perfetto-traces/ui-post-20261008-200620.pftrace -t 120s --app com.example.isitvegan sched freq idle am wm gfx view input binder_driver dalvik
```

- La capture a duré 120,369 s jusqu’au timeout configuré. Trace conservée dans `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/ui-post-20261008-200620.pftrace`, 13 737 977 octets, SHA-256 `F9D6C77E5E99A9D680CF1DE81CD8183FCFFC58A61BBB4EF20F067BEAD75DD072`. Copie depuis `/data/misc/perfetto-traces/ui-post-20261008-200620.pftrace` avec `adb -s AQ5003H044Q32600084 pull ...`.
- Analyse faite avec le `trace_processor_shell.exe` déjà présent dans le worktree pré-import. Trace Processor signale un seul paquet à timestamp négatif écarté (`trace_sorter_negative_timestamp_dropped`); les slices d’entrée, d’analyse et de frame citées ci-dessous sont présentes.

### Événement, analyse et affichage

Les slices ATrace de `com.example.isitvegan/.MainActivity` contiennent exactement un `dispatchInputEvent MotionEvent ACTION_DOWN` à `945897.818857 s` (source `0x1002`) et un `ACTION_UP` à `945897.954025 s`, soit un toucher de 135 ms. La table dérivée `__intrinsic_android_input_event_dispatch` reste vide dans cette trace; la preuve d’entrée est ici constituée des slices ATrace `receiveMessage`, `dispatchInputEvent` et `deliverInputEvent`, qui montrent un seul couple down/up dans l’application.

Le thread `DefaultDispatch`, TID `14372`, commence à courir à `945897.960 s`, environ 6 ms après ACTION_UP. Son dernier état Running observé est à `945909.511 s`; la somme des états Running sur cette période est 11 493,65 ms, sur une plage murale de 11,551 s. Cela indique que le dispatcher qui exécute le chemin `Dispatchers.Default` porte l’essentiel du délai et passe presque tout ce temps à tourner sur CPU, plutôt qu’à attendre le thread principal. Le code UI place analyse, formatage et `DiagnosticReport.build` dans ce chemin; cette trace ne les sépare pas. Une compilation JIT de `DiagnosticReport.build` est observée juste avant le rendu, à `945909.505579 s`, durée 39,805 ms sur le thread JIT.

Après la fin du travail sur Default, le premier `Choreographer#doFrame` associé à l’actualisation a lieu à `945909.525699 s`. Le `draw-VRI[MainActivity]` commence à `945909.535578 s` et dure 23,478 ms. La frame timeline réelle commence à `945909.525701 s` et dure 38,882 ms; elle est marquée `Late Present` / `App Deadline Missed`. Le délai entre ACTION_DOWN et la fin de cette première frame est donc d’environ **11,746 s** (≈ **11,75 s**). Le délai entre ACTION_UP et cette fin de frame est ≈ **11,611 s**. Cette mesure est un proxy trace du premier rendu après le calcul, et non un chronométrage instrumenté du callback Compose « résultat visible ».

Le thread principal traite le toucher dans environ 14,36 ms de `dispatchInputEvent` (dont une tranche `ViewPostImeInputStage` de 12,90 ms). Le rendu initial de la frame résultat est plus long qu’une frame cible de 16,7 ms et manque son échéance, mais il représente quelques dizaines de millisecondes après environ 11,5 s de calcul sur Default. La trace montre aussi 31 slices `NativeAlloc concurrent copying GC` sur `HeapTaskDaemon` pendant l’activité du worker, chacune de 41,88 à 52,95 ms (moyenne 44,90 ms); elles se chevauchent avec le travail et ne doivent pas être additionnées à la durée murale. Elles suggèrent une pression d’allocation/GC pendant cette analyse, sans prouver à elles seules quelle part du temps elles causent.

### Validité et limites

La trace contient le couple d’entrée physique au niveau de `MainActivity`, le travail soutenu sur `DefaultDispatch` et un premier frame result-related après ce travail : elle est utilisable pour localiser grossièrement le coût du clic. L’agent n’a effectué aucune interaction. La fenêtre inclut environ 31,6 s avant le toucher et une longue période après le rendu parce que la capture a pris fin à sa limite de 120 s sans retour de confirmation; aucune seconde capture n’a été lancée. L’absence de confirmation ne permet pas d’affirmer que l’utilisateur a visuellement validé le contenu du résultat, même si le frame de la `MainActivity` suit directement la fin du dispatcher et la construction du rapport diagnostique.

Le `trace_processor` a relevé un paquet à timestamp négatif; cela impose de la prudence sur des événements périphériques manquants, mais pas sur le down/up et les slices app utilisées ici. Perfetto n’est pas Simpleperf : les états `Running` localisent le thread et indiquent le temps passé à tourner, sans échantillonnage de fonctions ni ventilation parser/matcher/règles. Les 11,75 s ne sont pas à additionner aux mesures moteur instrumentées (surcharge et protocole distincts). La mesure est sur Nokia, pas Pixel. Les deux tests connus en régression n’ont pas été lancés.

**Réponse étayée :** sur ce clic, l’essentiel du délai est du travail CPU dans le chemin d’analyse sur `Dispatchers.Default` (~11,5 s presque continus); le thread principal n’attend pas l’analyse et le rendu final ajoute seulement des dizaines de millisecondes, avec une frame en retard. Le profil ne permet pas d’identifier quelle sous-phase moteur consomme le CPU ni d’attribuer les pauses aux GC.

## Simpleperf UI post-import — 8 octobre 2026 — tentative non exploitable

Une unique capture Simpleperf automatique de 30 s a été effectuée dans le worktree post-import `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, sur Nokia G42 5G (`AQ5003H044Q32600084`), Android 15. L’app déjà installée était la version debug `0.6.13.7` (versionCode 59), APK SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`; aucune compilation ni installation n’a été faite.

Commande exécutée :

```powershell
adb -s AQ5003H044Q32600084 shell simpleperf record --app com.example.isitvegan -o /data/local/tmp/simpleperf-ui-20261008-203354.data -e cpu-clock -f 4000 -g --duration 30
```

La capture a duré 29,9702 s, avec 9 877 échantillons et 0 perte. Les rapports skill `simpleperf-self.txt` et `simpleperf-children.txt` ont été générés dans `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-ui-20261008-203354/`, avec le brut `perf.data` (SHA-256 `5FB123C501AB5E2ABFC9E49395523EA710C2DB1D2AB7E2892D3A7001A17F3A64`), le journal, préflight, commande d’analyse et relevés après capture. Voir `simpleperf-summary.md` dans ce dossier pour les tableaux et limites.

Le profil n’est **pas exploitable pour identifier les fonctions coûteuses de l’analyse** : MainActivity représente 63,55 % des échantillons, RenderThread 26,66 %, DefaultExecutor 6,56 %, mais `DefaultDispatch` n’apparaît pas. Aucun symbole métier app-owned (service, parser, matcher, analyzers ou rapport de diagnostic) n’apparaît non plus. Les rapports sont dominés par ART/interpréteur (top self `artQuickToInterpreterBridge` 3,85 %; top inclusif `ExecuteSwitchImplAsm` 63,55 %) et par les piles de rendu Android/Compose. Simpleperf ne fournit pas ici de preuve du toucher manuel; on ne peut donc établir si le geste a eu lieu, seulement constater que le calcul moteur attendu n’est pas échantillonné. Le résultat n’est pas une preuve que le moteur est rapide ou lent.

Après capture : batterie 100 %, AC true, USB false, batterie 24,7 °C, Thermal Status 0; conditions avant capture non relevées. Build debug, échantillonnage CPU, symboles kernel restreints et absence probable de symboles Kotlin limitent l’attribution. Aucun autre profil, test, rebuild, réinstallation, modification de production, commit ou push n’a été effectué. La seule tentative est terminée et n’a pas été relancée.

## Simpleperf avec test UI ciblé — 8 octobre 2026 — non exploitable pour les hotspots métier

Dans le worktree post-import au commit `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, j’ai ajouté `RealLabelAnalysisUiCaptureTest#oneEditedRealLabelAnalysisClickEmitsCaptureMarkers`. Il utilise le flux OCR éditable, le texte propre exact du benchmark, ferme le clavier et fait un seul clic. Le runner de test uniquement a été installé; l’APK application post-import déjà présente n’a pas été reconstruite/réinstallée. Son SHA-256 reste `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`, version `0.6.13.7`, versionCode 59. Le test APK profilé avait le SHA-256 `15003D9D73F8C3FF02688B170D72D579948E386751F25DAA8B4E6F92FBB7DF9E`; aucun `5087.jpg` n’y figure.

Une capture Simpleperf `cpu-clock -f 4000 -g --duration 90` a enregistré 16,9393 s, 5 479 échantillons, 0 perdus. Les logs montrent `BEFORE_CLICK` à 21:13:38.528 et `AFTER_CLICK` à 21:13:38.594. L’attente a trouvé un verdict dans l’arbre Compose, mais l’assertion `assertIsDisplayed` a échoué car ce verdict était hors du viewport; `RESULT_DISPLAYED` manque. Après la capture, le test a été corrigé pour `performScrollTo()` avant l’assertion; cette correction compile localement mais n’a pas été relancée sur le Nokia.

La distribution comprend MainActivity 3 159 (57,66 %), RenderThread 1 432 (26,14 %), DefaultExecutor 330 (6,02 %), ConscryptStatsL 278 (5,07 %), et seulement cinq échantillons DefaultDispatch, tous sur le parking des workers. Aucun symbole app-owned d’analyse n’apparaît. Le profil est donc **non exploitable pour classer les fonctions coûteuses**. Comme le worker d’analyse n’est pas présent en exécution utile, je n’ai pas répété la capture. Voir [le résumé Simpleperf ciblé](SIMPLEPERF_TARGETED_ANALYSIS_UI_20261008.md) et les bruts sous `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-targeted-20261008-211039/`.

Le profileur CPU géré d’Android Studio (Kotlin/Java sampling/method mode) est la suite la plus adaptée si les frames métier restent absentes sous Simpleperf; aucune collecte supplémentaire n’a été faite. Détails des commandes, températures, hashes et limites dans le résumé.

## Profil Simpleperf synchronisé — 9 octobre 2026 — fonctions métier identifiées

Une collecte relue et corrigée a désormais réussi sur le Nokia post-import `3ec32d3914ad071b8435b9155e2e7d6467aeebd1`, application installée `0.6.13.7`/59. L’APK applicative installée conserve le SHA-256 `6EB2BE13DE5E5D87B70C7F26BA0C1E54BC9822574648F9BEF19FED77CC8D9B28`, vérifié directement sur l’appareil avant/après. Seul le runner androidTest a été compilé et installé; aucune donnée ou logique de production n’a changé.

Le test UI ciblé a réussi (`OK (1 test)`) avec READY, BEFORE_CLICK, AFTER_CLICK et RESULT_DISPLAYED du run `d2bf2bb146904755ae215471c26a256a`, PID `4487`. Les données Simpleperf portent ce même PID et couvrent toute la fenêtre. Enregistrement : 19,0192 s, 144 286 échantillons, 0 perdus, 5 236 piles tronquées; worker `DefaultDispatch` TID `4583` : 63 515 échantillons de calcul utile, tous entre les marqueurs clic et résultat. Le clic automatique unique utilise le texte corrigé exact; aucun ancien verdict ou résultat OCR en temps réel n’est accepté. Batterie 100 %, USB alimenté, AC false, température batterie 22,5 °C → 23,0 °C, Thermal Status 0 → 0.

Le hotspot est maintenant attribuable : `TextNormalizer.normalize` (JIT) **88,78 % inclusif / 0,27 % propre des échantillons worker**, `OriginQualifierRuleSet.extractAttached` **72,65 % inclusif**, `IngredientTreeParser.parse` **52,35 % inclusif**. Les piles relient ces appels à `kotlin.text.Regex.<init>`, `java.util.regex.Pattern.compile` et la compilation ICU. La source reconstruit les Regex constants dans chaque normalisation, et les règles d’origine répètent des normalisations dans leurs boucles. La construction `IngredientMatcher.<init>` ressort à 10,30 % inclusif worker; le matching JIT à 3,80 %. `DiagnosticReport.build` et le rendu du texte restent autour de 0,05 % chacun dans ce worker. Ces valeurs sont emboîtées et **ne s’additionnent pas**.

Le délai entre BEFORE_CLICK et l’assertion RESULT_DISPLAYED est 16,594 s **avec Simpleperf et les tests Compose**. Le thread principal du profil inclut du polling de l’infrastructure de test; sa répartition CPU ne caractérise pas le parcours manuel sans instrumentation. Cette valeur n’est pas une nouvelle baseline Android ni une comparaison avec le proxy Perfetto 11,75 s ou le benchmark moteur 10,5 s. Aucun effet pré/post import supplémentaire n’est calculé à partir de ce seul profil.

Les rapports, données brutes et scripts sont sous `.benchmark-worktrees/post-meat-3ec32d3/app/build/benchmark-artifacts/simpleperf-reviewed-20261009-105911/`, passage valide `attempt-03`. `perf.data` SHA-256 : `EE3AEDE183AD8403F5ECCFBA69FD1364E1F10AE749DAAA01213268DB838598EC`. Voir [le rapport synchronisé](SIMPLEPERF_SYNCHRONIZED_ATTEMPTS_20261008.md) pour les 19 validations locales, les deux échecs d’orchestration corrigés, les timestamps, hashes, commandes et piles complètes.

**Conclusion courante : le profil est exploitable et désigne principalement la normalisation répétée et la compilation Regex sollicitée par les règles d’origine dans le moteur.** Une piste ciblée serait de réutiliser les Regex constants et de pré-normaliser les valeurs invariantes hors des boucles; aucune optimisation n’est appliquée. Les suites et tests connus en régression restent hors périmètre. Aucune autre capture après succès, aucun commit ou push.
