#!/usr/bin/env python3
"""Seed the sb-hotel backend with dummy guests via POST /guests.

Guests are male or female (reflected only through the first-name pool
drawn from, since the Guest domain object has no gender attribute), aged
3-90, with addresses spread across every populated continent.

Usage:
    python3 scripts/seed/seed_guests.py [--base-url http://localhost:8080] [--count 200] [--seed 42]
"""

from __future__ import annotations

import argparse

import random

from common import DEFAULT_BASE_URL, post, random_address, random_name

MIN_AGE = 3
MAX_AGE = 90


def build_guest(rng: random.Random) -> dict:
    first, last = random_name(rng)
    return {
        "firstName": first,
        "lastName": last,
        "age": rng.randint(MIN_AGE, MAX_AGE),
        "address": random_address(rng),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--count", type=int, default=200)
    parser.add_argument("--seed", type=int, default=None)
    args = parser.parse_args()

    rng = random.Random(args.seed)
    created = 0
    for _ in range(args.count):
        guest = build_guest(rng)
        guest_id = post(args.base_url, "/guests", guest)
        created += 1
        if created % 40 == 0 or created == args.count:
            print(f"created {created}/{args.count} guests (last: {guest['firstName']} {guest['lastName']} - {guest_id})")

    print(f"Done. Created {created} guests at {args.base_url}")


if __name__ == "__main__":
    main()
