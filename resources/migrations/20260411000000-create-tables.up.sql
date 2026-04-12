CREATE TABLE play_events (
  id           BIGSERIAL PRIMARY KEY,
  track_id     TEXT        NOT NULL,
  track_name   TEXT        NOT NULL,
  track_number INTEGER,
  duration_ms  INTEGER,
  popularity   INTEGER,
  artist_id    TEXT        NOT NULL,
  artist_name  TEXT        NOT NULL,
  album_id     TEXT        NOT NULL,
  album_name   TEXT        NOT NULL,
  album_type   TEXT,
  album_tracks INTEGER,
  release_date TEXT,
  played_at    TIMESTAMPTZ NOT NULL,
  context_type TEXT,
  context_uri  TEXT
);

--;;

CREATE UNIQUE INDEX play_events_played_at_track_id_idx
  ON play_events (played_at, track_id);

--;;

CREATE TABLE ingestion_cursor (
  id            INTEGER PRIMARY KEY DEFAULT 1,
  last_played_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT single_row CHECK (id = 1)
);
