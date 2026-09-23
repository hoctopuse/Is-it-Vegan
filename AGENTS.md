# Is It Vegan? - Codex instructions

## General
- Work directly on the local repository.
- Prefer local inspection and local commands over asking for file contents.
- Do not request files that can be read locally.
- Do not reproduce large source files in responses.
- Keep responses concise.
- Return summaries and diffs rather than complete files unless necessary.

## Build and tests
- Run Gradle commands locally.
- After modifying Kotlin code, run the relevant unit tests locally.
- Prefer targeted tests before running the complete test suite.
- Do not paste complete Gradle logs into the conversation.
- Analyze logs locally and report only relevant errors.

## Repository
- Use git diff locally to inspect changes.
- Use git status locally.
- Search the repository locally using rg/find before requesting context.
- Never send the complete repository as context unnecessarily.

## Data
- ingredients.json is the reference ingredient database.
- Do not expand the ingredient database unless explicitly requested.
- Preserve aliases when modifying ingredient matching logic.

## Android
- Project language: Kotlin.
- Build system: Gradle.
- UI language: French.

## Documentation
- Toute modification significative de l'architecture, du pipeline OCR, du parsing, du moteur de verdict ou du format des données doit vérifier et, si nécessaire, mettre à jour `/docs`.

## Token/context efficiency
- Minimize context sent to the model.
- Read only files relevant to the current task.
- Prefer targeted searches over reading entire directories.
- Do not include build/, .gradle/, .idea/ or generated files in reasoning context.
