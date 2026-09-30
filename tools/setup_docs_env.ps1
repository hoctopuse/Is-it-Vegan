$ErrorActionPreference = "Stop"

if (-not (Test-Path ".venv-docs\Scripts\python.exe")) {
    py -3 -m venv .venv-docs
}

.\.venv-docs\Scripts\python.exe -m pip install -r requirements-docs.txt
.\.venv-docs\Scripts\python.exe -m mkdocs build --strict