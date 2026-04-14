-- resources/migrations/20260411000001-nullable-album.down.sql
ALTER TABLE play_events ALTER COLUMN album_id   SET NOT NULL;
ALTER TABLE play_events ALTER COLUMN album_name SET NOT NULL;
