#!/usr/bin/env python3
"""Seed the sb-hotel backend with dummy bookings via POST /bookings.

Fetches the rooms and guests that already exist on the server (created by
seed_rooms.py / seed_guests.py) and books them into non-overlapping stays
spread across the year 2026, 2-14 nights each. When a booking has more than
one guest, exactly one of them (an adult) is marked "isTheBooker".

Usage:
    python3 scripts/seed/seed_bookings.py [--base-url http://localhost:8080] [--count 500] [--seed 42]
"""

from __future__ import annotations

import argparse
import random
from datetime import date, timedelta

from common import DEFAULT_BASE_URL, get, post

YEAR = 2026
YEAR_START = date(YEAR, 1, 1)
YEAR_END = date(YEAR, 12, 31)

MIN_NIGHTS = 2
MAX_NIGHTS = 14

# number of guests -> relative likelihood
GUEST_COUNT_WEIGHTS = [(1, 40), (2, 35), (3, 15), (4, 10)]

MAX_ATTEMPTS_PER_BOOKING = 300


def room_capacity(room: dict) -> int:
    return room["sqm"] // 20


def overlaps(a_from: date, a_to: date, b_from: date, b_to: date) -> bool:
    return a_from < b_to and b_from < a_to


def pick_guest_count(rng: random.Random) -> int:
    values = [v for v, _ in GUEST_COUNT_WEIGHTS]
    weights = [w for _, w in GUEST_COUNT_WEIGHTS]
    return rng.choices(values, weights=weights, k=1)[0]


def pick_guests(rng: random.Random, adults: list[dict], all_guests: list[dict], count: int) -> list[dict]:
    booker = rng.choice(adults)
    others_pool = [g for g in all_guests if g["id"] != booker["id"]]
    others = rng.sample(others_pool, k=min(count - 1, len(others_pool)))
    chosen = [booker, *others]
    return [
        {
            "id": g["id"],
            "firstName": g["firstName"],
            "lastName": g["lastName"],
            "age": g["age"],
            "address": g.get("address"),
            "isTheBooker": g["id"] == booker["id"],
        }
        for g in chosen
    ]


def random_stay(rng: random.Random) -> tuple[date, date]:
    nights = rng.randint(MIN_NIGHTS, MAX_NIGHTS)
    latest_start = YEAR_END - timedelta(days=nights)
    span = (latest_start - YEAR_START).days
    start = YEAR_START + timedelta(days=rng.randint(0, span))
    return start, start + timedelta(days=nights)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--count", type=int, default=500)
    parser.add_argument("--seed", type=int, default=None)
    args = parser.parse_args()

    rng = random.Random(args.seed)

    rooms = [r for r in get(args.base_url, "/rooms") if r["state"] == "FREE"]
    guests = get(args.base_url, "/guests")
    adults = [g for g in guests if g["age"] >= 18]

    if not rooms:
        raise SystemExit("No FREE rooms found on the server - run seed_rooms.py first")
    if not adults:
        raise SystemExit("No adult guests found on the server - run seed_guests.py first")

    rooms_by_capacity: dict[int, list[dict]] = {}
    for room in rooms:
        rooms_by_capacity.setdefault(room_capacity(room), []).append(room)

    booked_intervals: dict[str, list[tuple[date, date]]] = {r["id"]: [] for r in rooms}

    created = 0
    skipped = 0
    for _ in range(args.count):
        guest_count = pick_guest_count(rng)
        eligible_rooms = [r for cap, rs in rooms_by_capacity.items() if cap >= guest_count for r in rs]
        if not eligible_rooms:
            skipped += 1
            continue

        booked = False
        for _ in range(MAX_ATTEMPTS_PER_BOOKING):
            room = rng.choice(eligible_rooms)
            start, end = random_stay(rng)
            if any(overlaps(start, end, b_from, b_to) for b_from, b_to in booked_intervals[room["id"]]):
                continue

            payload = {
                "guests": pick_guests(rng, adults, guests, guest_count),
                "room": {"id": room["id"]},
                "from": start.isoformat(),
                "to": end.isoformat(),
            }
            post(args.base_url, "/bookings", payload)
            booked_intervals[room["id"]].append((start, end))
            created += 1
            booked = True
            if created % 50 == 0 or created == args.count:
                print(f"created {created}/{args.count} bookings")
            break

        if not booked:
            skipped += 1

    print(f"Done. Created {created} bookings ({skipped} skipped due to scheduling conflicts) at {args.base_url}")


if __name__ == "__main__":
    main()
