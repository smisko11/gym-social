-- =====================================================================
--  Početni podaci za šifarnike (grupe mišića i osnovne vežbe).
--  Teretane NISU ovde — njih unosiš ručno, sa pravim adresama.
-- =====================================================================

INSERT INTO muscle_group (name) VALUES
    ('Grudi'), ('Leđa'), ('Ramena'), ('Biceps'), ('Triceps'), ('Podlaktice'),
    ('Kvadriceps'), ('Zadnja loža'), ('Gluteus'), ('Listovi'), ('Stomak');

INSERT INTO exercise (name, is_bodyweight) VALUES
    ('Bench press', false),
    ('Kosi bench press', false),
    ('Propadanja', true),
    ('Sklekovi', true),
    ('Mrtvo dizanje', false),
    ('Zgibovi', true),
    ('Veslanje šipkom', false),
    ('Lat pulldown', false),
    ('Vojnički potisak', false),
    ('Odručenje bučicama', false),
    ('Biceps pregib šipkom', false),
    ('Triceps ekstenzija na sajli', false),
    ('Čučanj', false),
    ('Rumunsko mrtvo dizanje', false),
    ('Nožna presa', false),
    ('Hip thrust', false),
    ('Podizanje na prste', false),
    ('Plank', true);

-- veza vežba <-> grupa mišića (is_primary = glavna grupa)
INSERT INTO exercise_muscle_group (exercise_id, muscle_group_id, is_primary)
SELECT e.id, m.id, v.is_primary
FROM (VALUES
    ('Bench press',                 'Grudi',       true),
    ('Bench press',                 'Triceps',     false),
    ('Bench press',                 'Ramena',      false),
    ('Kosi bench press',            'Grudi',       true),
    ('Kosi bench press',            'Ramena',      false),
    ('Propadanja',                  'Triceps',     true),
    ('Propadanja',                  'Grudi',       false),
    ('Sklekovi',                    'Grudi',       true),
    ('Sklekovi',                    'Triceps',     false),
    ('Mrtvo dizanje',               'Leđa',        true),
    ('Mrtvo dizanje',               'Zadnja loža', true),
    ('Mrtvo dizanje',               'Gluteus',     false),
    ('Zgibovi',                     'Leđa',        true),
    ('Zgibovi',                     'Biceps',      false),
    ('Veslanje šipkom',             'Leđa',        true),
    ('Veslanje šipkom',             'Biceps',      false),
    ('Lat pulldown',                'Leđa',        true),
    ('Lat pulldown',                'Biceps',      false),
    ('Vojnički potisak',            'Ramena',      true),
    ('Vojnički potisak',            'Triceps',     false),
    ('Odručenje bučicama',          'Ramena',      true),
    ('Biceps pregib šipkom',        'Biceps',      true),
    ('Biceps pregib šipkom',        'Podlaktice',  false),
    ('Triceps ekstenzija na sajli', 'Triceps',     true),
    ('Čučanj',                      'Kvadriceps',  true),
    ('Čučanj',                      'Gluteus',     false),
    ('Rumunsko mrtvo dizanje',      'Zadnja loža', true),
    ('Rumunsko mrtvo dizanje',      'Gluteus',     false),
    ('Nožna presa',                 'Kvadriceps',  true),
    ('Hip thrust',                  'Gluteus',     true),
    ('Podizanje na prste',          'Listovi',     true),
    ('Plank',                       'Stomak',      true)
) AS v (exercise_name, muscle_name, is_primary)
JOIN exercise     e ON e.name = v.exercise_name
JOIN muscle_group m ON m.name = v.muscle_name;
