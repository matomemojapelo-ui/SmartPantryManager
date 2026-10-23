# Smart Pantry Manager

An Android application (Java) that helps users reduce food waste by tracking
the ingredients they have at home and suggesting recipes they can cook
using strictly those ingredients only — no shopping trip required.

## Assignment
Mobile App Development 700 — Practical Assignment
Student: Matome Joseph Mojapelo - 402310987

## Database Choice
SQLite (via `SQLiteOpenHelper`)
Chosen because:
- On-device — no network dependency, works offline
- Zero setup cost — no server, no API keys, no billing
- Well suited to a single-user pantry app with local data
- Full CRUD via standard ContentValues / Cursor APIs

## Setup & Run Instructions
1. Install Android Studio Hedgehog (2023.1.1) or newer.
2. Clone this repo:
