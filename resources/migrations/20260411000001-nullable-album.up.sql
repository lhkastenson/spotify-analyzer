-- resources/migrations/20260411000001-nullable-album.up.sql
ALTER TABLE play_events ALTER COLUMN album_id   DROP NOT NULL;
--;;
ALTER TABLE play_events ALTER COLUMN album_name DROP NOT NULL;
