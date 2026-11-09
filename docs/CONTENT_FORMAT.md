# Content format

All course content lives in `app/src/main/assets/content/`. Nothing else needs to change to add a unit: the path, exercises, tests and reviews are all built from these files.

## course.json

Lists the levels in order and, for each, its unit files in order.

```json
{
  "version": 1,
  "levels": [
    { "id": "elementary", "order": 1, "title": "Elementary", "titleFa": "مقدماتی",
      "units": ["elementary/01-family.json", "elementary/02-jobs.json"] }
  ]
}
```

A level with an empty `units` list shows as "coming soon". A unit opens when the unit before it is passed, across levels.

## A unit file

```json
{
  "id": "elementary-02-jobs",
  "order": 2,
  "title": "Jobs",
  "titleFa": "شغل‌ها",
  "illustration": "jobs",
  "color": "pink",
  "descriptionFa": "اسم شغل‌ها و حرف زدن درباره‌ی کار.",
  "referenceRows": [],
  "words": [
    {
      "id": "jobs-doctor",
      "text": "doctor",
      "meaningFa": "پزشک، دکتر",
      "partOfSpeech": "noun",
      "phonetic": "/ˈdɒk.tər/",
      "example": "My aunt is a doctor at the city hospital.",
      "tipFa": "اختیاری",
      "illustration": null,
      "wordFamily": [],
      "collocations": []
    }
  ],
  "gapQuestions": [
    { "id": "jobs-gap-1", "sentence": "A ___ works in a hospital.", "answer": "doctor", "hintFa": "اختیاری" }
  ]
}
```

| Field | Required | Notes |
| --- | --- | --- |
| `id` | yes | Unique across the whole course and **never changed** once released: progress and backups refer to it. |
| `order` | yes | Position inside its level. |
| `title`, `titleFa` | yes | English and Persian names. |
| `illustration` | yes | One of the keys below. |
| `color` | yes | `periwinkle`, `mint`, `butter`, `pink`, `sky` or `lilac`. |
| `descriptionFa` | no | One line on the unit page. |
| `referenceRows` | no | Lines shown above gap questions (for example a family tree). |
| `words[].id` | yes | Unique and permanent, like the unit id. Convention: `<topic>-<word>`. |
| `words[].example` | yes | Should contain the word itself; the gap exercise cuts it out of this sentence. |
| `words[].tipFa` | no | Shown in a yellow tip box on the learn card. |
| `gapQuestions` | no | Hand-written fill-in-the-blank questions, used before example sentences. `sentence` contains one `___`; `answer` must be one of the unit's words. |

Generated exercises: meaning (4 choices), match (groups of 4), gap (hand-written first, then from examples), listening (text-to-speech), and the unit test (every word once, mixed types). Distractors come from the same unit, so a unit needs at least 4 words with different meanings.

## Illustration keys

`animals`, `backup`, `bell`, `books`, `brain`, `bullseye`, `calendar`, `chart-increasing`, `check`, `clothes`, `cross`, `family`, `fire`, `food`, `graduation-cap`, `headphone`, `home`, `jobs`, `light-bulb`, `locked`, `open-book`, `party-popper`, `sparkles`, `speaker`, `sport`, `star`, `student`, `travel`, `trophy`, `weather`.

To add one: put the PNG in `core/designsystem/src/main/res/drawable-nodpi/illus_<name>.png` (underscores), add an entry to `Illustration` in `core/model` and to `drawableRes()` in `core/designsystem`.

## Checking content

Run `python3 tools/validate_content.py` after editing content. It checks unique ids, known illustrations and colors, that each example contains its word, that gap answers are words of the unit, and duplicate meanings.

## Writing content

Write original definitions, examples and exercises. Word lists are fine to take from any source; example sentences and exercises copied from a published book are not.
