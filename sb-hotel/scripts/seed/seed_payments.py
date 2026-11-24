#!/usr/bin/env python3
"""Seed the sb-hotel backend with dummy payments via POST /payments.

Fetches the bookings that already exist on the server (created by
seed_bookings.py) and creates a payment for a random subset of them,
tagged with a plausible payment-provider ID and a payment date shortly
before the booking's check-in.

Usage:
    python3 scripts/seed/seed_payments.py [--base-url http://localhost:8080] [--count 300] [--fraction 0.8] [--seed 42]
"""

from __future__ import annotations

import argparse
import random
from datetime import date, datetime, timedelta

from common import DEFAULT_BASE_URL, get, post

# days before check-in a payment is typically made
MIN_DAYS_BEFORE_CHECKIN = 0
MAX_DAYS_BEFORE_CHECKIN = 30

PAYMENT_PROVIDERS = [
    "stripe", "paypal", "adyen", "braintree", "worldpay", "square", "klarna",
]


def random_provider_id(rng: random.Random) -> str:
    provider = rng.choice(PAYMENT_PROVIDERS)
    return f"{provider}_{rng.randrange(10**12, 10**13):x}"


def random_payment_date(rng: random.Random, check_in: date) -> int:
    days_before = rng.randint(MIN_DAYS_BEFORE_CHECKIN, MAX_DAYS_BEFORE_CHECKIN)
    paid_on = check_in - timedelta(days=days_before)
    moment = datetime(paid_on.year, paid_on.month, paid_on.day, rng.randint(0, 23), rng.randint(0, 59))
    return int(moment.timestamp() * 1000)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL)
    parser.add_argument("--count", type=int, default=None, help="max number of payments to create (default: all selected bookings)")
    parser.add_argument("--fraction", type=float, default=0.8, help="fraction of bookings that receive a payment (default 0.8)")
    parser.add_argument("--seed", type=int, default=None)
    args = parser.parse_args()

    rng = random.Random(args.seed)

    bookings = get(args.base_url, "/bookings")
    if not bookings:
        raise SystemExit("No bookings found on the server - run seed_bookings.py first")

    selected = [b for b in bookings if rng.random() < args.fraction]
    rng.shuffle(selected)
    if args.count is not None:
        selected = selected[:args.count]

    created = 0
    for booking in selected:
        check_in = date.fromisoformat(booking["from"])
        payload = {
            "booking": booking,
            "paymentProviderId": random_provider_id(rng),
            "paymentDate": random_payment_date(rng, check_in),
        }
        post(args.base_url, "/payments", payload)
        created += 1
        if created % 50 == 0 or created == len(selected):
            print(f"created {created}/{len(selected)} payments")

    print(f"Done. Created {created} payments at {args.base_url}")


if __name__ == "__main__":
    main()
