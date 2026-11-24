"""Shared helpers and reference data for the sb-hotel dummy-data seed scripts.

No third-party dependencies - only the Python standard library, so these
scripts run anywhere Python 3 is installed.
"""

from __future__ import annotations

import json
import random
import urllib.error
import urllib.request

DEFAULT_BASE_URL = "http://localhost:8080"


def post(base_url: str, path: str, payload: dict):
    return _request(base_url, path, "POST", payload)


def get(base_url: str, path: str):
    return _request(base_url, path, "GET", None)


def _request(base_url: str, path: str, method: str, payload):
    url = f"{base_url.rstrip('/')}{path}"
    data = json.dumps(payload).encode("utf-8") if payload is not None else None
    headers = {"Content-Type": "application/json"} if data is not None else {}
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req) as resp:
            body = resp.read().decode("utf-8")
            return json.loads(body) if body else None
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {url} failed [{exc.code}]: {detail}") from exc
    except urllib.error.URLError as exc:
        raise RuntimeError(f"{method} {url} failed to connect: {exc.reason}") from exc


# --- Reference data used to generate guests --------------------------------
#
# The Guest domain object has no explicit gender attribute, so "male or
# female" is only reflected in which first-name pool a guest is drawn from.

MALE_FIRST_NAMES = [
    "Ivan", "Marko", "Luka", "Josip", "Ante", "Tin", "Filip", "Petar", "Stjepan", "Mario",
    "David", "Michael", "James", "Robert", "Thomas", "Daniel", "Matthew", "Ryan", "Jack", "Henry",
    "Ahmed", "Mohammed", "Hassan", "Omar", "Khalid",
    "Chen", "Wei", "Jian", "Kenji", "Hiroshi", "Haruto",
    "Kwame", "Sipho", "Tendai", "Chidi",
    "Diego", "Carlos", "Pedro", "Mateus", "Santiago", "Joaquin",
]

FEMALE_FIRST_NAMES = [
    "Ana", "Ivana", "Petra", "Nina", "Ema", "Lucija", "Klara", "Dora", "Iva", "Marta",
    "Emma", "Olivia", "Isabella", "Mia", "Charlotte", "Amelia", "Sophia", "Grace", "Chloe", "Zoe",
    "Fatima", "Aisha", "Zainab", "Layla", "Noor",
    "Mei", "Yuki", "Sakura", "Hana", "Jiwoo",
    "Amara", "Wanjiru", "Ngozi", "Aminata",
    "Valentina", "Camila", "Lucia", "Sofia", "Isabela", "Renata",
]

LAST_NAMES = [
    "Horvat", "Kovacevic", "Novak", "Babic", "Maric", "Peric", "Juric", "Vidovic", "Kovac", "Knezevic",
    "Smith", "Johnson", "Brown", "Williams", "Taylor", "Muller", "Schmidt", "Fischer", "Weber",
    "Dubois", "Martin", "Garcia", "Rodriguez", "Fernandez", "Silva", "Santos", "Oliveira", "Costa",
    "Ivanov", "Petrov", "Novikov", "Sokolov",
    "Nguyen", "Tran", "Kim", "Park", "Wang", "Li", "Zhang", "Tanaka", "Suzuki", "Sato",
    "Okafor", "Mwangi", "Osei", "Diallo",
    "Ahmadi", "Hassan", "Ali", "Khan",
]

# continent -> list of (city, ISO country code, base postal code, street name)
ADDRESSES_BY_CONTINENT = {
    "Europe": [
        ("Zagreb", "HR", 10000, "Ilica"),
        ("Berlin", "DE", 10115, "Alexanderplatz"),
        ("Paris", "FR", 75001, "Rue de Rivoli"),
        ("Madrid", "ES", 28013, "Gran Via"),
        ("Rome", "IT", 184, "Via del Corso"),
        ("Vienna", "AT", 1010, "Kaerntner Strasse"),
        ("Amsterdam", "NL", 1012, "Damrak"),
        ("Warsaw", "PL", 100, "Nowy Swiat"),
        ("Stockholm", "SE", 11120, "Drottninggatan"),
        ("Athens", "GR", 10557, "Ermou"),
        ("Dublin", "IE", 1, "Grafton Street"),
        ("Lisbon", "PT", 1100, "Avenida da Liberdade"),
    ],
    "Asia": [
        ("Tokyo", "JP", 1000001, "Chuo Dori"),
        ("Beijing", "CN", 100000, "Wangfujing Street"),
        ("Mumbai", "IN", 400001, "Marine Drive"),
        ("Seoul", "KR", 4524, "Myeongdong-gil"),
        ("Bangkok", "TH", 10200, "Sukhumvit Road"),
        ("Jakarta", "ID", 10110, "Jalan Thamrin"),
        ("Dubai", "AE", 11111, "Sheikh Zayed Road"),
        ("Singapore", "SG", 188964, "Orchard Road"),
        ("Manila", "PH", 1000, "Roxas Boulevard"),
        ("Hanoi", "VN", 100000, "Hoan Kiem"),
    ],
    "Africa": [
        ("Cairo", "EG", 11511, "Tahrir Street"),
        ("Lagos", "NG", 100001, "Marina Road"),
        ("Nairobi", "KE", 100, "Kenyatta Avenue"),
        ("Johannesburg", "ZA", 2000, "Commissioner Street"),
        ("Casablanca", "MA", 20000, "Boulevard Mohammed V"),
        ("Accra", "GH", 233, "Oxford Street"),
        ("Tunis", "TN", 1000, "Avenue Habib Bourguiba"),
        ("Addis Ababa", "ET", 1000, "Bole Road"),
    ],
    "North America": [
        ("New York", "US", 10001, "5th Avenue"),
        ("Toronto", "CA", 100, "Yonge Street"),
        ("Mexico City", "MX", 6000, "Paseo de la Reforma"),
        ("Chicago", "US", 60601, "Michigan Avenue"),
        ("Vancouver", "CA", 200, "Robson Street"),
        ("Havana", "CU", 10100, "Malecon"),
    ],
    "South America": [
        ("Sao Paulo", "BR", 1000, "Avenida Paulista"),
        ("Buenos Aires", "AR", 1000, "Avenida Corrientes"),
        ("Lima", "PE", 15001, "Avenida Larco"),
        ("Bogota", "CO", 110111, "Carrera Septima"),
        ("Santiago", "CL", 8320000, "Avenida Providencia"),
        ("Montevideo", "UY", 11000, "18 de Julio"),
    ],
    "Oceania": [
        ("Sydney", "AU", 2000, "George Street"),
        ("Auckland", "NZ", 1010, "Queen Street"),
        ("Melbourne", "AU", 3000, "Collins Street"),
        ("Perth", "AU", 6000, "Hay Street"),
        ("Suva", "FJ", 679, "Victoria Parade"),
    ],
}

# Room names are inspired by the original README example (a single town
# name, e.g. "Jajce") - reused here with a room number suffix for uniqueness.
ROOM_TOWNS = [
    "Jajce", "Mostar", "Pula", "Rovinj", "Zadar", "Sibenik", "Split", "Dubrovnik",
    "Rijeka", "Varazdin", "Osijek", "Vukovar", "Karlovac", "Cakovec", "Bjelovar",
    "Koprivnica", "Krapina", "Gospic", "Otocac", "Slavonski Brod", "Vinkovci",
    "Djakovo", "Nasice", "Virovitica", "Pakrac", "Daruvar", "Ilok", "Ludbreg",
    "Samobor", "Zapresic", "Velika Gorica", "Sesvete", "Trogir", "Omis",
    "Makarska", "Metkovic", "Ploce", "Knin", "Sinj", "Imotski", "Vis", "Hvar",
    "Korcula", "Lastovo", "Mljet", "Cres", "Krk", "Rab", "Pag", "Biograd",
    "Nin", "Opatija", "Porec", "Umag", "Novigrad", "Motovun", "Buzet", "Labin",
]


def random_name(rng: random.Random) -> tuple[str, str]:
    gender = rng.choice(("male", "female"))
    first = rng.choice(MALE_FIRST_NAMES if gender == "male" else FEMALE_FIRST_NAMES)
    last = rng.choice(LAST_NAMES)
    return first, last


def random_address(rng: random.Random) -> dict:
    continent = rng.choice(list(ADDRESSES_BY_CONTINENT))
    city, country, postal_base, street = rng.choice(ADDRESSES_BY_CONTINENT[continent])
    return {
        "street": f"{street} {rng.randint(1, 200)}",
        "postalCode": postal_base + rng.randint(0, 99),
        "city": city,
        "state": country,
    }
