-- =====================================================================
--  Društvena mreža za teretanu — početna šema (MVP)
--  Baza: PostgreSQL 16 (izabrana zbog pgvector ekstenzije za kasniji recommender)
--  Fajl je pisan kao Flyway migracija: src/main/resources/db/migration/V1__init_schema.sql
--
--  Konvencije:
--   * imena tabela i kolona: engleski, snake_case (Java entiteti će biti engleski)
--   * primarni ključ: BIGINT GENERATED ALWAYS AS IDENTITY  (JPA: GenerationType.IDENTITY)
--   * vreme: TIMESTAMPTZ (čuva trenutak u UTC-u, bez zabune oko vremenskih zona)
--   * "enumi": VARCHAR + CHECK  (JPA: @Enumerated(EnumType.STRING))
--   * redosled tabela: prvo one na koje drugi pokazuju, pa one koje pokazuju na njih
--   * updated_at se NE menja sam: u JPA entitetu stavi @UpdateTimestamp
--   * redni brojevi (position, set_number) kreću od 1
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. ŠIFARNICI  (popunjavaš ih ti, korisnik samo bira)
-- ---------------------------------------------------------------------

CREATE TABLE gym (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    city        VARCHAR(100)  NOT NULL,
    address     VARCHAR(255)  NOT NULL,
    latitude    NUMERIC(9, 6),                       -- za kasniju pretragu "teretane blizu mene"
    longitude   NUMERIC(9, 6),
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_gym_name_address UNIQUE (name, address),
    CONSTRAINT ck_gym_lat CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_gym_lng CHECK (longitude BETWEEN -180 AND 180)
);
CREATE INDEX ix_gym_city ON gym (city);


CREATE TABLE muscle_group (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(50) NOT NULL,
    CONSTRAINT uq_muscle_group_name UNIQUE (name)
);


CREATE TABLE exercise (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    is_bodyweight  BOOLEAN      NOT NULL DEFAULT false,   -- zgib, sklek... kilaža u seriji = dodatni teret
    CONSTRAINT uq_exercise_name UNIQUE (name)
);


-- M:N  vežba <-> grupa mišića  (mrtvo dizanje gađa leđa, zadnju ložu, gluteus...)
CREATE TABLE exercise_muscle_group (
    exercise_id      BIGINT  NOT NULL REFERENCES exercise (id)     ON DELETE CASCADE,
    muscle_group_id  BIGINT  NOT NULL REFERENCES muscle_group (id) ON DELETE RESTRICT,
    is_primary       BOOLEAN NOT NULL DEFAULT true,               -- glavna ili pomoćna grupa
    PRIMARY KEY (exercise_id, muscle_group_id)
);
CREATE INDEX ix_emg_muscle_group ON exercise_muscle_group (muscle_group_id);


-- ---------------------------------------------------------------------
-- 2. NALOG I PROFIL  (1:1, deljeni primarni ključ)
-- ---------------------------------------------------------------------

-- "user" je rezervisana reč u PostgreSQL-u, zato app_user
CREATE TABLE app_user (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(255) NOT NULL,
    username       VARCHAR(30)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,                         -- BCrypt hash, NIKAD čista lozinka
    role           VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled        BOOLEAN      NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_app_user_role     CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_app_user_username CHECK (username ~ '^[a-zA-Z0-9_.]{3,30}$')
);
-- jedinstvenost bez obzira na velika/mala slova: Marko@gmail.com == marko@gmail.com
CREATE UNIQUE INDEX uq_app_user_email    ON app_user (lower(email));
CREATE UNIQUE INDEX uq_app_user_username ON app_user (lower(username));


CREATE TABLE user_profile (
    user_id           BIGINT PRIMARY KEY REFERENCES app_user (id) ON DELETE CASCADE,  -- PK = FK  =>  najviše jedan profil
    display_name      VARCHAR(60),
    bio               VARCHAR(500),
    city              VARCHAR(100),
    goal              VARCHAR(20),
    experience_level  VARCHAR(20),
    home_gym_id       BIGINT REFERENCES gym (id) ON DELETE SET NULL,
    avatar_url        VARCHAR(500),
    visibility        VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_profile_goal       CHECK (goal IN ('MUSCLE_GAIN', 'STRENGTH', 'FAT_LOSS')),
    CONSTRAINT ck_profile_experience CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_profile_visibility CHECK (visibility IN ('PUBLIC', 'FRIENDS', 'PRIVATE'))
);
CREATE INDEX ix_profile_home_gym ON user_profile (home_gym_id);
CREATE INDEX ix_profile_city     ON user_profile (city);


-- ---------------------------------------------------------------------
-- 3. ODNOSI MEĐU KORISNICIMA
-- ---------------------------------------------------------------------

-- Praćenje: jednosmerno, bez odobrenja
CREATE TABLE follow (
    follower_id  BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,  -- ko prati
    followee_id  BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,  -- koga prati
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT ck_follow_not_self CHECK (follower_id <> followee_id)
);
CREATE INDEX ix_follow_followee ON follow (followee_id);   -- "ko mene prati"


-- Prijateljstvo: dvosmerno, sa zahtevom
--   Odbijanje zahteva i raskid prijateljstva = BRISANJE reda.
--   (Da postoji status DECLINED, red bi ostao zauvek i jedinstveni par
--    bi sprečio da iko od njih dvoje ikad ponovo pošalje zahtev.)
CREATE TABLE friendship (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    requester_id  BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    addressee_id  BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    accepted_at   TIMESTAMPTZ,
    CONSTRAINT ck_friendship_not_self CHECK (requester_id <> addressee_id),
    CONSTRAINT ck_friendship_status   CHECK (status IN ('PENDING', 'ACCEPTED')),
    CONSTRAINT ck_friendship_accepted CHECK ((status = 'ACCEPTED') = (accepted_at IS NOT NULL))
);
-- A->B i B->A su ISTI par: manji id uvek ide prvi, pa duplikat pukne
CREATE UNIQUE INDEX uq_friendship_pair
    ON friendship (LEAST(requester_id, addressee_id), GREATEST(requester_id, addressee_id));
CREATE INDEX ix_friendship_addressee ON friendship (addressee_id, status);   -- "moji dolazni zahtevi"
CREATE INDEX ix_friendship_requester ON friendship (requester_id, status);   -- "moji poslati zahtevi" i lista prijatelja


-- ---------------------------------------------------------------------
-- 4. SADRŽAJ
-- ---------------------------------------------------------------------

CREATE TABLE post (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id   BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    content     TEXT,                                    -- može biti NULL ako post ima samo slike
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_post_content_len CHECK (char_length(content) <= 2200)
);
CREATE INDEX ix_post_author_created ON post (author_id, created_at DESC);   -- feed jednog korisnika
CREATE INDEX ix_post_created        ON post (created_at DESC);              -- globalni feed


CREATE TABLE post_media (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id     BIGINT       NOT NULL REFERENCES post (id) ON DELETE CASCADE,
    media_type  VARCHAR(10)  NOT NULL,
    url         VARCHAR(500) NOT NULL,                   -- fajl je na disku/S3-u, u bazi samo putanja
    position    SMALLINT     NOT NULL,                   -- redosled u carousel-u (1, 2, 3...)
    CONSTRAINT ck_post_media_type     CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT ck_post_media_position CHECK (position >= 1),
    CONSTRAINT uq_post_media_position UNIQUE (post_id, position)
);


-- M:N korisnik <-> post
CREATE TABLE post_like (
    post_id     BIGINT      NOT NULL REFERENCES post (id)     ON DELETE CASCADE,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (post_id, user_id)                       -- isti lajk dvaput = nemoguće
);
CREATE INDEX ix_post_like_user ON post_like (user_id);


CREATE TABLE comment (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    post_id            BIGINT        NOT NULL REFERENCES post (id)     ON DELETE CASCADE,
    author_id          BIGINT        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    parent_comment_id  BIGINT,                                         -- odgovor na komentar (NULL = komentar na post)
    content            VARCHAR(1000) NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT ck_comment_not_blank CHECK (char_length(trim(content)) > 0),
    CONSTRAINT ck_comment_not_self  CHECK (parent_comment_id <> id),
    -- potrebno da bi složeni FK ispod mogao da pokazuje na par (id, post_id)
    CONSTRAINT uq_comment_id_post   UNIQUE (id, post_id),
    -- odgovor mora biti na ISTOM postu kao komentar na koji odgovara
    CONSTRAINT fk_comment_parent_same_post FOREIGN KEY (parent_comment_id, post_id)
        REFERENCES comment (id, post_id) ON DELETE CASCADE
);
CREATE INDEX ix_comment_post   ON comment (post_id, created_at);
CREATE INDEX ix_comment_parent ON comment (parent_comment_id);
CREATE INDEX ix_comment_author ON comment (author_id);


-- ---------------------------------------------------------------------
-- 5. RASPORED
-- ---------------------------------------------------------------------

CREATE TABLE workout_schedule (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    name        VARCHAR(60) NOT NULL,                    -- npr. "PPL", "Upper/Lower"
    is_active   BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_schedule_user ON workout_schedule (user_id);
-- delimični UNIQUE indeks: korisnik može imati više rasporeda, ali samo JEDAN aktivan
CREATE UNIQUE INDEX uq_schedule_one_active ON workout_schedule (user_id) WHERE is_active;


CREATE TABLE schedule_day (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    schedule_id  BIGINT       NOT NULL REFERENCES workout_schedule (id) ON DELETE CASCADE,
    day_of_week  SMALLINT     NOT NULL,                  -- 1 = ponedeljak ... 7 = nedelja (kao java.time.DayOfWeek)
    note         VARCHAR(200),
    CONSTRAINT ck_schedule_day_dow CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT uq_schedule_day     UNIQUE (schedule_id, day_of_week)
);
-- dan bez ijedne grupe mišića = dan odmora


-- M:N dan rasporeda <-> grupa mišića  (ponedeljak = grudi + triceps)
CREATE TABLE schedule_day_muscle_group (
    schedule_day_id  BIGINT NOT NULL REFERENCES schedule_day (id) ON DELETE CASCADE,
    muscle_group_id  BIGINT NOT NULL REFERENCES muscle_group (id) ON DELETE RESTRICT,
    PRIMARY KEY (schedule_day_id, muscle_group_id)
);
CREATE INDEX ix_sdmg_muscle_group ON schedule_day_muscle_group (muscle_group_id);


-- Plan vežbi za dan: "ponedeljak: bench press 4 x 8-10, kosi bench 3 x 10-12"
CREATE TABLE schedule_day_exercise (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    schedule_day_id  BIGINT   NOT NULL REFERENCES schedule_day (id) ON DELETE CASCADE,
    exercise_id      BIGINT   NOT NULL REFERENCES exercise (id)     ON DELETE RESTRICT,
    position         SMALLINT NOT NULL,                  -- redosled vežbi u danu
    target_sets      SMALLINT,                           -- NULL = nije zadato
    target_reps_min  SMALLINT,
    target_reps_max  SMALLINT,
    CONSTRAINT uq_sde_position UNIQUE (schedule_day_id, position),
    CONSTRAINT ck_sde_position CHECK (position >= 1),
    CONSTRAINT ck_sde_sets     CHECK (target_sets BETWEEN 1 AND 20),
    CONSTRAINT ck_sde_reps     CHECK (target_reps_min >= 1 AND target_reps_max >= target_reps_min)
);
CREATE INDEX ix_sde_exercise ON schedule_day_exercise (exercise_id);


-- ---------------------------------------------------------------------
-- 6. ZAJEDNIČKI TRENING  (= "status": "idem na trening u 18h")
--    Pre odrađenog treninga, jer odrađen trening pokazuje na njega.
-- ---------------------------------------------------------------------

CREATE TABLE group_workout (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organizer_id      BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    gym_id            BIGINT      NOT NULL REFERENCES gym (id)      ON DELETE RESTRICT,
    planned_start     TIMESTAMPTZ NOT NULL,              -- "idem u 18h"
    ended_at          TIMESTAMPTZ,                       -- popuni se kad se trening završi
    max_participants  SMALLINT,                          -- NULL = bez ograničenja
    description       VARCHAR(300),
    status            VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_gw_status   CHECK (status IN ('SCHEDULED', 'CANCELLED', 'FINISHED')),
    CONSTRAINT ck_gw_max      CHECK (max_participants >= 2),
    CONSTRAINT ck_gw_end      CHECK (ended_at IS NULL OR ended_at > planned_start),
    CONSTRAINT ck_gw_finished CHECK (status <> 'FINISHED' OR ended_at IS NOT NULL)   -- završen trening mora imati kraj
);
CREATE INDEX ix_gw_organizer ON group_workout (organizer_id, planned_start DESC);
-- "ko trenira u mojoj teretani danas" — indeksiramo samo aktivne
CREATE INDEX ix_gw_gym_upcoming ON group_workout (gym_id, planned_start) WHERE status = 'SCHEDULED';


-- koji deo tela se radi na zajedničkom treningu
CREATE TABLE group_workout_muscle_group (
    group_workout_id  BIGINT NOT NULL REFERENCES group_workout (id) ON DELETE CASCADE,
    muscle_group_id   BIGINT NOT NULL REFERENCES muscle_group (id)  ON DELETE RESTRICT,
    PRIMARY KEY (group_workout_id, muscle_group_id)
);
CREATE INDEX ix_gwmg_muscle_group ON group_workout_muscle_group (muscle_group_id);   -- "ko danas radi noge"


-- Učešće: i poziv i zahtev u JEDNOJ tabeli
--   INVITE  -> organizator je pozvao korisnika   -> odobrava KORISNIK
--   REQUEST -> korisnik je tražio da se pridruži -> odobrava ORGANIZATOR
CREATE TABLE group_workout_participant (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    group_workout_id  BIGINT      NOT NULL REFERENCES group_workout (id) ON DELETE CASCADE,
    user_id           BIGINT      NOT NULL REFERENCES app_user (id)      ON DELETE CASCADE,
    type              VARCHAR(10) NOT NULL,
    status            VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    responded_at      TIMESTAMPTZ,
    CONSTRAINT ck_gwp_type     CHECK (type   IN ('INVITE', 'REQUEST')),
    CONSTRAINT ck_gwp_status   CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED')),
    CONSTRAINT ck_gwp_response CHECK ((status = 'PENDING') = (responded_at IS NULL)),
    CONSTRAINT uq_gwp_once     UNIQUE (group_workout_id, user_id)       -- korisnik je na treningu najviše jednom
);
CREATE INDEX ix_gwp_user ON group_workout_participant (user_id);
-- za @Scheduled posao koji traži istekle zahteve
CREATE INDEX ix_gwp_pending ON group_workout_participant (group_workout_id) WHERE status = 'PENDING';


-- ---------------------------------------------------------------------
-- 7. ODRAĐEN TRENING  (trening -> vežba u treningu -> serija)
-- ---------------------------------------------------------------------

CREATE TABLE workout_session (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id           BIGINT      NOT NULL REFERENCES app_user (id)      ON DELETE CASCADE,
    gym_id            BIGINT      REFERENCES gym (id)                    ON DELETE SET NULL,
    group_workout_id  BIGINT      REFERENCES group_workout (id)          ON DELETE SET NULL,  -- "radio sam sa Markom"
    started_at        TIMESTAMPTZ NOT NULL,
    ended_at          TIMESTAMPTZ,
    note              VARCHAR(500),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_ws_end CHECK (ended_at IS NULL OR ended_at > started_at)
);
CREATE INDEX ix_ws_user_started ON workout_session (user_id, started_at DESC);
CREATE INDEX ix_ws_gym          ON workout_session (gym_id);
CREATE INDEX ix_ws_group        ON workout_session (group_workout_id);


CREATE TABLE session_exercise (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id   BIGINT   NOT NULL REFERENCES workout_session (id) ON DELETE CASCADE,
    exercise_id  BIGINT   NOT NULL REFERENCES exercise (id)        ON DELETE RESTRICT,
    position     SMALLINT NOT NULL,                     -- redosled vežbi u treningu (1, 2, 3...)
    CONSTRAINT ck_se_position CHECK (position >= 1),
    CONSTRAINT uq_se_position UNIQUE (session_id, position)
);
CREATE INDEX ix_se_exercise ON session_exercise (exercise_id);


CREATE TABLE exercise_set (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_exercise_id  BIGINT       NOT NULL REFERENCES session_exercise (id) ON DELETE CASCADE,
    set_number           SMALLINT     NOT NULL,         -- 1., 2., 3. serija
    weight_kg            NUMERIC(6, 2),                 -- NULL = bez tega; kod vežbi sa sopstvenom težinom = dodatni teret
    reps                 SMALLINT     NOT NULL,
    CONSTRAINT ck_set_number CHECK (set_number > 0),
    CONSTRAINT ck_set_weight CHECK (weight_kg BETWEEN 0 AND 600),    -- svetski rekordi su ~500kg; hvata greške u kucanju (850 umesto 85)
    CONSTRAINT ck_set_reps   CHECK (reps BETWEEN 1 AND 1000),
    CONSTRAINT uq_set_number UNIQUE (session_exercise_id, set_number)
);


-- ---------------------------------------------------------------------
-- 8. NOTIFIKACIJE  (pristup 1: posebna kolona za svaku metu)
-- ---------------------------------------------------------------------

CREATE TABLE notification (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_id      BIGINT      NOT NULL REFERENCES app_user (id)      ON DELETE CASCADE,  -- kome stiže
    actor_id          BIGINT      REFERENCES app_user (id)               ON DELETE CASCADE,  -- ko je to uradio (NULL = sistem)
    type              VARCHAR(40) NOT NULL,
    post_id           BIGINT      REFERENCES post (id)                   ON DELETE CASCADE,
    comment_id        BIGINT      REFERENCES comment (id)                ON DELETE CASCADE,
    group_workout_id  BIGINT      REFERENCES group_workout (id)          ON DELETE CASCADE,
    is_read           BOOLEAN     NOT NULL DEFAULT false,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_notification_type CHECK (type IN (
        'NEW_FOLLOWER',
        'FRIEND_REQUEST', 'FRIEND_ACCEPTED',
        'POST_LIKED', 'POST_COMMENTED', 'COMMENT_REPLIED',
        'WORKOUT_INVITE', 'WORKOUT_JOIN_REQUEST',
        'WORKOUT_REQUEST_ACCEPTED', 'WORKOUT_REQUEST_DECLINED', 'WORKOUT_REQUEST_EXPIRED',
        'WORKOUT_CANCELLED'
    )),
    -- najviše jedna meta (post, komentar ili trening) — "korisnik" je već actor_id
    CONSTRAINT ck_notification_one_target CHECK (num_nonnulls(post_id, comment_id, group_workout_id) <= 1),
    -- svaki tip mora pokazivati na pravu vrstu mete
    CONSTRAINT ck_notification_target_matches_type CHECK (
           (type IN ('NEW_FOLLOWER', 'FRIEND_REQUEST', 'FRIEND_ACCEPTED')
                AND num_nonnulls(post_id, comment_id, group_workout_id) = 0)
        OR (type = 'POST_LIKED'                          AND post_id IS NOT NULL)
        OR (type IN ('POST_COMMENTED', 'COMMENT_REPLIED') AND comment_id IS NOT NULL)   -- komentar zna svoj post
        OR (type LIKE 'WORKOUT\_%'                       AND group_workout_id IS NOT NULL)
    ),
    -- samo sistemska notifikacija (automatsko isticanje) nema aktera
    CONSTRAINT ck_notification_actor CHECK (actor_id IS NOT NULL OR type = 'WORKOUT_REQUEST_EXPIRED'),
    -- ne obaveštavaš sam sebe (lajk na sopstveni post)
    CONSTRAINT ck_notification_not_self CHECK (actor_id <> recipient_id)
);
CREATE INDEX ix_notification_recipient ON notification (recipient_id, created_at DESC);
-- brojač nepročitanih (crvena tačkica) — indeks samo nad nepročitanim
CREATE INDEX ix_notification_unread ON notification (recipient_id) WHERE NOT is_read;
CREATE INDEX ix_notification_post   ON notification (post_id);
CREATE INDEX ix_notification_comment ON notification (comment_id);
CREATE INDEX ix_notification_gw     ON notification (group_workout_id);
CREATE INDEX ix_notification_actor  ON notification (actor_id);
