# SpringBoot

## Rooms

POST http://localhost:8080/rooms
```json
{
  "name": "Jajce",
  "price": 123.45,
  "sqm": 167,
  "canBeUsedWithHandicaps": false,
  "availableSince": 20010
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