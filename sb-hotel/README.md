# SpringBoot

### API Docs

Swagger: http://localhost:8080/swagger-ui/index.html
OpenAPI: http://localhost:8080/v3/api-docs

## Rooms

POST http://localhost:8080/rooms
```json
{
  "name": "Jajce",
  "price": 123.45,
  "sqm": 167,
  "canBeUsedWithHandicaps": false,
  "availableSince": 2010
}
```

POST http://localhost:8080/bookings
```json
{
  "guests": [
    {
      "firstName": "Sam",
      "lastName": "Newman",
      "age": 34,
      "address": {
        "street": "Ulica Petra Preradovića 225",
        "postalCode": 31400,
        "city": "Đakovo",
        "state": "HR"
      }
    },
    {
      "firstName": "Ana",
      "lastName": "Newman",
      "age": 15
    }
  ],
  "room": {
    "id": "04c1efa6-f549-4af9-8fa3-89c723b05a53"
  },
  "date": [2024,10,3]
}
```

POST http://localhost:8080/guests
```json
{
  "firstName": "Sam",
  "lastName": "Oldman",
  "age": 34,
  "address": {
    "street": "Ulica Petra Preradovića 225",
    "postalCode": 31400,
    "city": "Đakovo",
    "state": "HR"
  }
}
```

## Dummy data

`scripts/seed/` has standalone Python 3 scripts (standard library only, no
pip install needed) that seed the running app with dummy data via its REST
API: 100 rooms, 200 guests (all continents, ages 3-90), and 500 bookings
spread across 2026 (2-14 nights each, non-overlapping per room).

Start the app first, then from the `sb-hotel` directory run:

```shell
python3 scripts/seed/seed_all.py
```

This runs `seed_rooms.py`, `seed_guests.py`, and `seed_bookings.py` in
order (bookings reference rooms/guests created by the first two, fetched
live via `GET /rooms` and `GET /guests`). They can also be run
individually, e.g. to add more bookings on top of existing data:

```shell
python3 scripts/seed/seed_rooms.py --count 50
python3 scripts/seed/seed_guests.py --count 100
python3 scripts/seed/seed_bookings.py --count 200
```

Common options on every script:
- `--base-url` - target server (default `http://localhost:8080`)
- `--seed` - fix the random seed for reproducible output
- `--count` - how many entities to create (default 100/200/500 respectively)