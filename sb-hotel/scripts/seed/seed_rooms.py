#!/usr/bin/env python3
"""Seed the sb-hotel backend with dummy rooms via POST /rooms.

Creates rooms with varied name/price/size/accessibility/state/availability
attributes. The "combinations" attribute is intentionally left untouched
(the server fills it with its own default value).

Usage:
    python3 scripts/seed/seed_rooms.py [--base-url http://localhost:8080] [--count 100] [--seed 42]
"""

from __future__ import annotations

import argparse
import random
from datetime import date, timedelta

from common import DEFAULT_BASE_URL, ROOM_TOWNS, post

# label, selection weight, sqm range, price range
# sqm drives Room.maxNumberOfGuests() = sqm // 20, so the ranges are chosen
# to land cleanly on a max-guests tier (1, 2, 3, 4, 5+).
ROOM_CATEGORIES = [
    ("Single", 30, (20, 39), (45, 75)),
    ("Double", 35, (40, 59), (70, 120)),
    ("Family Room", 18, (60, 79), (120, 180)),
    ("Suite", 12, (80, 99), (180, 280)),
    ("Presidential Suite", 5, (100, 140), (300, 500)),
]

AVAILABLE_SINCE_START = date(2015, 1, 1)
AVAILABLE_SINCE_END = date(2026, 6, 1)


def random_available_since(rng: random.Random) -> str:
    span = (AVAILABLE_SINCE_END - AVAILABLE_SINCE_START).days
    return (AVAILABLE_SINCE_START + timedelta(days=rng.randint(0, span))).isoformat()


def pick_category(rng: random.Random):
    labels = [c[0] for c in ROOM_CATEGORIES]
    weights = [c[1] for c in ROOM_CATEGORIES]
    label = rng.choices(labels, weights=weights, k=1)[0]
    return next(c for c in ROOM_CATEGORIES if c[0] == label)


def build_room(rng: random.Random, room_number: int) -> dict:
    _, _, sqm_range, price_range = pick_category(rng)
    town = ROOM_TOWNS[room_number % len(ROOM_TOWNS)]
    return {
        "name": f"{town} {room_number}",
        "defaultPrice": round(rng.uniform(*price_range), 2),
        "sqm": rng.randint(*sqm_range),
        "canBeUsedWithHandicaps": rng.random() < 0.15,
        "state": "FREE" if rng.random() < 0.9 else "BLOCKED",
        "availableSince": random_available_since(rng),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--count", type=int, default=100)
    parser.add_argument("--seed", type=int, default=None)
    args = parser.parse_args()

    rng = random.Random(args.seed)
    created = 0
    for i in range(args.count):
        room = build_room(rng, 100 + i)
        result = post(args.base_url, "/rooms", room)
        created += 1
        if created % 20 == 0 or created == args.count:
            print(f"created {created}/{args.count} rooms (last: {result['name']} - {result['id']})")

    print(f"Done. Created {created} rooms at {args.base_url}")


if __name__ == "__main__":
    main()
