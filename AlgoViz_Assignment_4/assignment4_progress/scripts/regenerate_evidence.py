"""Rebuild and capture genuine evidence on the machine running this script.
Requires JDK 21, Maven, curl, and scripts/requirements-tools.txt in this Python env.
Run: python scripts/regenerate_evidence.py (from assignment4_progress).
"""
from pathlib import Path
import os
import platform
import shutil
import subprocess
import sys
import tempfile
from datetime import datetime, timezone

SERVICE = Path(__file__).resolve().parents[1]

def main():
    executables = {name: shutil.which(name) for name in ("java", "mvn", "curl")}
    missing = [name for name, path in executables.items() if path is None]
    if missing:
        raise RuntimeError("Install these tools and put them on PATH: " + ", ".join(missing))
    with tempfile.TemporaryDirectory(prefix="algoviz-evidence-") as directory:
        staging = Path(directory)
        env = dict(os.environ, ALGOVIZ_EVIDENCE_DIR=str(staging))
        def run(args, filename):
            print("Running:", " ".join(str(a) for a in args), flush=True)
            with (staging / filename).open("w", encoding="utf-8") as log:
                result = subprocess.run(args, cwd=SERVICE, env=env, stdout=log,
                                        stderr=subprocess.STDOUT, text=True)
            if result.returncode:
                print((staging / filename).read_text(encoding="utf-8", errors="replace"))
                raise RuntimeError("Command failed; existing evidence was not replaced.")
        run([executables["mvn"], "-B", "clean", "package"], "junit-output.txt")
        run([sys.executable, "scripts/validate_contract.py"], "openapi-validation.txt")
        run([sys.executable, "scripts/capture_evidence.py"], "network-checks.txt")
        with (staging / "environment.txt").open("w", encoding="utf-8") as log:
            log.write("Generated UTC: " + datetime.now(timezone.utc).isoformat() + "\n")
            log.write("Working directory: " + str(SERVICE) + "\n")
            log.write("Platform: " + platform.platform() + "\n")
            for args in ([executables["java"], "-version"], [executables["mvn"], "-version"],
                         [executables["curl"], "--version"], [sys.executable, "--version"]):
                subprocess.run(args, cwd=SERVICE, env=env, stdout=log,
                               stderr=subprocess.STDOUT, check=True)
        (staging / "PROVENANCE.md").write_text(
            "# Evidence provenance\n\n"
            "These files are unedited outputs generated on the machine running "
            "scripts/regenerate_evidence.py. See environment.txt for the actual "
            "working directory, timestamp and tool versions. They are laptop "
            "evidence only if this script was run on that laptop.\n", encoding="utf-8")
        destination = SERVICE / "evidence"
        destination.mkdir(exist_ok=True)
        for file in staging.iterdir():
            shutil.copy2(file, destination / file.name)
    print("PASS: build, OpenAPI and live HTTP evidence regenerated in", destination)

if __name__ == "__main__":
    main()
