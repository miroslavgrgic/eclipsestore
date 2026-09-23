#!/usr/bin/env python3
"""Run seed_rooms.py, seed_guests.py, and seed_bookings.py in order.

Bookings reference rooms and guests by ID, so this order matters: rooms and
guests must exist before bookings are created.

Usage:
    python3 scripts/seed/seed_all.py [--base-url http://localhost:8080] [--seed 42]
"""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path

from common import DEFAULT_BASE_URL

SCRIPT_DIR = Path(__file__).parent


def run(script: str, args: argparse.Namespace) -> None:
    cmd = [sys.executable, str(SCRIPT_DIR / script), "--base-url", args.base_url]
    if args.seed is not None:
        cmd += ["--seed", str(args.seed)]
    subprocess.run(cmd, check=True)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--seed", type=int, default=None)
    args = parser.parse_args()

    run("seed_rooms.py", args)
    run("seed_guests.py", args)
    run("seed_bookings.py", args)


if __name__ == "__main__":
    main()
